package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.pay.products.util.PaymentStatus.SUBMITTED;

public class PaymentFinder {

    private final PaymentDao paymentDao;
    private final LinksDecorator linksDecorator;
    private final PublicApiRestClient publicApiRestClient;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PaymentFinder.class);

    @Inject
    public PaymentFinder(PaymentDao paymentDao, LinksDecorator linksDecorator, PublicApiRestClient publicApiRestClient) {
        this.paymentDao = paymentDao;
        this.linksDecorator = linksDecorator;
        this.publicApiRestClient = publicApiRestClient;
    }

    public List<Payment> findByProductExternalId(String productExternalId) {
        return findPaymentEntitiesByProductExternalId(productExternalId)
                .stream()
                .map(paymentEntity -> {
                    Payment payment = queryGovUKPaymentStatus(paymentEntity);
                    return linksDecorator.decorate(payment);
                })
                .collect(Collectors.toList());
    }

    public Optional<Payment> findByExternalId(String paymentExternalId) {
        return findPaymentEntity(paymentExternalId).map(paymentEntity -> {
            Payment payment = queryGovUKPaymentStatus(paymentEntity);
            return linksDecorator.decorate(payment);
        });
    }

    @Transactional
    public Optional<PaymentEntity> findPaymentEntity(String paymentExternalId) {
        return paymentDao.findByExternalId(paymentExternalId);
    }

    @Transactional
    public List<PaymentEntity> findPaymentEntitiesByProductExternalId(String productExternalId) {
        return paymentDao.findByProductExternalId(productExternalId);
    }

    private Payment queryGovUKPaymentStatus(PaymentEntity paymentEntity) {
        Payment payment = paymentEntity.toPayment();
        if (payment.getStatus() == SUBMITTED) {
            try {
                Optional<PaymentResponse> paymentResponseOptional =
                        publicApiRestClient.getPayment(paymentEntity.getProductEntity().getPayApiToken(),
                                paymentEntity.getGovukPaymentId());

                if (paymentResponseOptional.isPresent()) {
                    PaymentResponse paymentResponse = paymentResponseOptional.get();
                    String status = paymentResponse.getState().getStatus();
                    payment.setGovukStatus(status);
                }
            } catch (PublicApiResponseErrorException ex) {
                logger.error(format("Error while trying to query publicapi with %s: %s",paymentEntity.getExternalId(), ex.getMessage()));
            }
        }
        return payment;

    }
}
