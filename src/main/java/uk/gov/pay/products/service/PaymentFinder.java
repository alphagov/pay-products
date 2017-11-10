package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.pay.products.util.PaymentStatus.SUBMITTED;

public class PaymentFinder {

    private final PaymentDao paymentDao;
    private final LinksDecorator linksDecorator;
    private final PublicApiRestClient publicApiRestClient;

    @Inject
    public PaymentFinder(PaymentDao paymentDao, LinksDecorator linksDecorator, PublicApiRestClient publicApiRestClient) {
        this.paymentDao = paymentDao;
        this.linksDecorator = linksDecorator;
        this.publicApiRestClient = publicApiRestClient;
    }

    @Transactional
    public List<Payment> findByProductExternalId(String productExternalId) {
        List<Payment> payments = paymentDao.findByProductExternalId(productExternalId)
                .stream()
                .map(paymentEntity -> linksDecorator.decorate(paymentEntity.toPayment()))
                .collect(Collectors.toList());
        return payments;
    }

    public Optional<Payment> findByExternalId(String paymentExternalId){
        return  findPaymentEntity(paymentExternalId).map(paymentEntity -> {
                Payment payment = paymentEntity.toPayment();
                if (payment.getStatus() == SUBMITTED) {
                    Optional<PaymentResponse> paymentResponseOptional =
                            publicApiRestClient.getPayment(paymentEntity.getProductEntity().getPayApiToken(),
                                    paymentEntity.getGovukPaymentId());
                    if (paymentResponseOptional.isPresent()) {
                        PaymentResponse paymentResponse = paymentResponseOptional.get();
                        String status = paymentResponse.getState().getStatus();
                        payment.setGovukStatus(status);
                    }
                }
                return linksDecorator.decorate(payment);
        });
    }

    @Transactional
    public Optional<PaymentEntity> findPaymentEntity(String paymentExternalId){
        return paymentDao.findByExternalId(paymentExternalId);
    }
}
