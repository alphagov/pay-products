package uk.gov.pay.products.service;

import com.google.inject.persist.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.persistence.dao.PaymentDao;

import jakarta.inject.Inject;

import static java.lang.String.format;

public class PaymentUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentUpdater.class);

    public static final String REDACTED_REFERENCE_NUMBER = "****************";

    private final PaymentDao paymentDao;

    @Inject
    public PaymentUpdater(PaymentDao paymentDao) {
        this.paymentDao = paymentDao;
    }

    @Transactional
    public void redactReference(String govukPaymentId) {
        paymentDao.findByGovukPaymentId(govukPaymentId).ifPresentOrElse(payment -> {
            payment.setReferenceNumber(REDACTED_REFERENCE_NUMBER);
            paymentDao.merge(payment);
        }, () -> LOGGER.warn(format("Payment with govuk payment id not found, nothing to redact."), govukPaymentId));
    }

    @Transactional
    public void redactReferenceByExternalId(String externalId) {
        paymentDao.findByExternalId(externalId).ifPresentOrElse(payment -> {
            payment.setReferenceNumber(REDACTED_REFERENCE_NUMBER);
            paymentDao.merge(payment);
        }, () -> LOGGER.warn("Payment with external ID not found, nothing to redact."));
    }
}
