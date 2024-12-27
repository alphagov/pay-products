package uk.gov.pay.products.service;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicapi.PaymentRequest;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.exception.BadPaymentRequestException;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.exception.PaymentCreatorNotFoundException;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.service.transaction.NonTransactionalOperation;
import uk.gov.pay.products.service.transaction.TransactionContext;
import uk.gov.pay.products.service.transaction.TransactionFlow;
import uk.gov.pay.products.service.transaction.TransactionalOperation;
import uk.gov.pay.products.util.PaymentStatus;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.model.Source;

import jakarta.inject.Inject;

import static java.lang.String.format;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUserFriendlyReference;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;
import static uk.gov.service.payments.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;

public class PaymentCreator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Provider<TransactionFlow> transactionFlowProvider;
    private final ProductDao productDao;
    private final PaymentDao paymentDao;
    private final PublicApiRestClient publicApiRestClient;
    private final LinksDecorator linksDecorator;
    private final ProductsConfiguration productsConfiguration;
    private static final int MAX_NUMBER_OF_RETRY_FOR_UNIQUE_REF_NUMBER = 3;
    private final PaymentFactory paymentFactory;

    @Inject
    public PaymentCreator(Provider<TransactionFlow> transactionFlowProvider, ProductDao productDao, PaymentDao paymentDao,
                          PublicApiRestClient publicApiRestClient, LinksDecorator linksDecorator,
                          ProductsConfiguration productsConfiguration, PaymentFactory paymentFactory) {
        this.transactionFlowProvider = transactionFlowProvider;
        this.productDao = productDao;
        this.paymentDao = paymentDao;
        this.publicApiRestClient = publicApiRestClient;
        this.linksDecorator = linksDecorator;
        this.productsConfiguration = productsConfiguration;
        this.paymentFactory = paymentFactory;
    }

    public Payment doCreate(String productExternalId, Long priceOverride, String reference) {
        PaymentEntity paymentEntity = transactionFlowProvider.get()
                .executeNext(beforePaymentCreation(productExternalId, reference))
                .executeNext(paymentCreation(priceOverride))
                .executeNext(afterPaymentCreation())
                .complete().get(PaymentEntity.class);

        if (paymentEntity.getStatus() == PaymentStatus.ERROR) {
            if (CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR.equals(paymentEntity.getErrorCode())) {
                paymentFactory.paymentUpdater().redactReferenceByExternalId(paymentEntity.getExternalId());
            }

            throw new PaymentCreationException(paymentEntity.getProductEntity().getExternalId(),
                    paymentEntity.getErrorStatusCode(),
                    paymentEntity.getErrorCode(),
                    paymentEntity.getErrorDescription());
        }
        return linksDecorator.decorate(paymentEntity.toPayment());
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> beforePaymentCreation(String productExternalId, String userDefinedReference) {
        return context -> {
            logger.info("Creating a new payment for product external id {}", productExternalId);
            ProductEntity productEntity = productDao.findByExternalId(productExternalId)
                    .orElseThrow(() -> new PaymentCreatorNotFoundException(productExternalId));
            if (productEntity.getReferenceEnabled()) {
                if (isEmpty(userDefinedReference)) {
                    throw new BadPaymentRequestException("User defined reference is enabled but missing");
                }
                return mergePaymentEntityWithoutReferenceCheck(setupPaymentEntity(productEntity, userDefinedReference));
            }

            PaymentEntity paymentEntity = setupPaymentEntity(productEntity, randomUserFriendlyReference());

            return mergePaymentEntityWithReferenceNumberCheck(paymentEntity);
        };
    }

    private PaymentEntity mergePaymentEntityWithoutReferenceCheck(PaymentEntity paymentEntity) {
        paymentDao.persist(paymentEntity);
        return paymentEntity;
    }

    private PaymentEntity mergePaymentEntityWithReferenceNumberCheck(PaymentEntity paymentEntity) {
        for (int i = 0; i < MAX_NUMBER_OF_RETRY_FOR_UNIQUE_REF_NUMBER; i++) {
            String reference = randomUserFriendlyReference();
            if (paymentDao.findByGatewayAccountIdAndReferenceNumber(paymentEntity.getGatewayAccountId(), reference).isEmpty()) {
                paymentEntity.setReferenceNumber(reference);
                paymentDao.persist(paymentEntity);
                return paymentEntity;
            }
        }

        String exceptionMsg = format("Too many conflicts generating unique user friendly reference numbers for gateway account %s", paymentEntity.getGatewayAccountId());
        RuntimeException runtimeException = new RuntimeException(exceptionMsg);
        logger.error(exceptionMsg, runtimeException);
        throw runtimeException;
    }

    private NonTransactionalOperation<TransactionContext, PaymentEntity> paymentCreation(Long priceOverride) {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            ProductEntity productEntity = paymentEntity.getProductEntity();
            String returnUrl = format("%s/%s", productsConfiguration.getProductsUiConfirmUrl(), paymentEntity.getExternalId());
            Long paymentPrice = priceOverride != null ? priceOverride : productEntity.getPrice();
            boolean isMoto = productEntity.getType() == ProductType.AGENT_INITIATED_MOTO;
            Source source = productEntity.getType() == ProductType.AGENT_INITIATED_MOTO ? Source.CARD_AGENT_INITIATED_MOTO : Source.CARD_PAYMENT_LINK;

            PaymentRequest paymentRequest = new PaymentRequest(
                    paymentPrice,
                    paymentEntity.getReferenceNumber(),
                    productEntity.getName(),
                    returnUrl,
                    productEntity.getLanguage(),
                    isMoto,
                    productEntity.toProductMetadataMap(),
                    source);

            try {
                PaymentResponse paymentResponse = publicApiRestClient.createPayment(productEntity.getPayApiToken(), paymentRequest);

                paymentEntity.setGovukPaymentId(paymentResponse.getPaymentId());
                paymentEntity.setNextUrl(getNextUrl(paymentResponse));
                paymentEntity.setStatus(PaymentStatus.SUBMITTED);
                paymentEntity.setAmount(paymentResponse.getAmount());
                logger.info(
                        "Payment creation for product external id {} successful",
                        paymentEntity.getProductEntity().getExternalId(),
                        kv(PAYMENT_EXTERNAL_ID, paymentEntity.getGovukPaymentId()),
                        kv("product_external_id", paymentEntity.getProductEntity().getExternalId())
                );
            } catch (PublicApiResponseErrorException e) {
                logger.warn("Payment creation for product external id {} failed {}", paymentEntity.getProductEntity().getExternalId(), e.getMessage());
                paymentEntity.setStatus(PaymentStatus.ERROR);
                paymentEntity.setErrorStatusCode(e.getErrorStatus());
                paymentEntity.setErrorCode(e.getCode());
                paymentEntity.setErrorDescription(e.getDescription());
            }

            return paymentEntity;
        };
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> afterPaymentCreation() {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            paymentDao.merge(paymentEntity);

            logger.info("Payment creation for product external id {} completed", paymentEntity.getProductEntity().getExternalId());
            return paymentEntity;
        };
    }

    private String getNextUrl(PaymentResponse paymentResponse) {
        if ((paymentResponse.getLinks() != null) &&
                (paymentResponse.getLinks().getNextUrl() != null)) {
            return paymentResponse.getLinks().getNextUrl().getHref();
        }
        return "";
    }

    private PaymentEntity setupPaymentEntity(ProductEntity productEntity, String referenceToBeUsed) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setExternalId(randomUuid());
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(PaymentStatus.CREATED);
        paymentEntity.setGatewayAccountId(productEntity.getGatewayAccountId());
        paymentEntity.setReferenceNumber(referenceToBeUsed);
        return paymentEntity;
    }
}
