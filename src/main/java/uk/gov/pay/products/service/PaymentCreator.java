package uk.gov.pay.products.service;

import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicapi.PaymentRequest;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.config.ProductsConfiguration;
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

import javax.inject.Inject;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentCreator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Provider<TransactionFlow> transactionFlowProvider;
    private final ProductDao productDao;
    private final PaymentDao paymentDao;
    private final PublicApiRestClient publicApiRestClient;
    private final LinksDecorator linksDecorator;
    private final ProductsConfiguration productsConfiguration;


    @Inject
    public PaymentCreator(Provider<TransactionFlow> transactionFlowProvider, ProductDao productDao, PaymentDao paymentDao,
                          PublicApiRestClient publicApiRestClient, LinksDecorator linksDecorator,
                          ProductsConfiguration productsConfiguration) {
        this.transactionFlowProvider = transactionFlowProvider;
        this.productDao = productDao;
        this.paymentDao = paymentDao;
        this.publicApiRestClient = publicApiRestClient;
        this.linksDecorator = linksDecorator;
        this.productsConfiguration = productsConfiguration;
    }

    public Payment doCreate(String productExternalId) {
        PaymentEntity paymentEntity = transactionFlowProvider.get()
                .executeNext(beforePaymentCreation(productExternalId))
                .executeNext(paymentCreation())
                .executeNext(afterPaymentCreation())
                .complete().get(PaymentEntity.class);

        if (paymentEntity.getStatus() == PaymentStatus.ERROR) {
            throw new PaymentCreationException(paymentEntity.getProductEntity().getExternalId());
        }
        return linksDecorator.decorate(paymentEntity.toPayment());
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> beforePaymentCreation(String productExternalId) {
        return context -> {
            logger.info("Creating a new payment for product external id {}", productExternalId);
            ProductEntity productEntity = productDao.findByExternalId(productExternalId)
                    .orElseThrow(() -> new PaymentCreatorNotFoundException(productExternalId));

            PaymentEntity paymentEntity = new PaymentEntity();
            paymentEntity.setExternalId(randomUuid());
            paymentEntity.setProductEntity(productEntity);
            paymentEntity.setStatus(PaymentStatus.CREATED);
            paymentDao.persist(paymentEntity);

            return paymentEntity;
        };
    }

    private NonTransactionalOperation<TransactionContext, PaymentEntity> paymentCreation() {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            ProductEntity productEntity = paymentEntity.getProductEntity();

            String returnUrl = isBlank(productEntity.getReturnUrl())
                    ? format("%s/%s", productsConfiguration.getProductsUiConfirmUrl(), paymentEntity.getExternalId())
                    : productEntity.getReturnUrl();

            PaymentRequest paymentRequest = new PaymentRequest(
                    productEntity.getPrice(),
                    productEntity.getExternalId(),
                    productEntity.getName(),
                    returnUrl);

            try {
                PaymentResponse paymentResponse = publicApiRestClient.createPayment(productEntity.getPayApiToken(), paymentRequest);

                paymentEntity.setGovukPaymentId(paymentResponse.getPaymentId());
                paymentEntity.setNextUrl(getNextUrl(paymentResponse));
                paymentEntity.setStatus(PaymentStatus.SUBMITTED);
                paymentEntity.setAmount(paymentResponse.getAmount());
                logger.info("Payment creation for product external id {} successful {}", paymentEntity.getProductEntity().getExternalId(), paymentEntity);
            } catch (PublicApiResponseErrorException e) {
                logger.error("Payment creation for product external id {} failed {}", paymentEntity.getProductEntity().getExternalId(), e);
                paymentEntity.setStatus(PaymentStatus.ERROR);
            }

            return paymentEntity;
        };
    }

    private TransactionalOperation<TransactionContext, PaymentEntity> afterPaymentCreation() {
        return context -> {
            PaymentEntity paymentEntity = context.get(PaymentEntity.class);
            paymentDao.merge(paymentEntity);

            logger.info("Payment creation for product external id {} completed {}", paymentEntity.getProductEntity().getExternalId());
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
}
