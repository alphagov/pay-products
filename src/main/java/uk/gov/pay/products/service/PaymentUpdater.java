package uk.gov.pay.products.service;

import com.google.inject.persist.Transactional;
import org.slf4j.*;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import javax.inject.Inject;
import java.util.Optional;

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
}
