package uk.gov.pay.products.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.config.ExpungeHistoricalDataConfig;
import uk.gov.pay.products.persistence.dao.PaymentDao;

import javax.inject.Inject;
import java.time.Clock;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;

public class PaymentDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentDeleter.class);
    
    private ExpungeHistoricalDataConfig expungeHistoricalDataConfig;
    private PaymentDao paymentDao;
    private Clock clock;

    @Inject
    public PaymentDeleter(ExpungeHistoricalDataConfig expungeHistoricalDataConfig, PaymentDao paymentDao, Clock clock) {
        this.expungeHistoricalDataConfig = expungeHistoricalDataConfig;
        this.paymentDao = paymentDao;
        this.clock = clock;
    }

    public void deletePayments() {
        if (!expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()) {
            LOGGER.info("Expunging of historical data is not enabled.");
            return;
        }
        
        var maxDate = clock.instant().minus(expungeHistoricalDataConfig.getExpungeDataOlderThanDays(), ChronoUnit.DAYS).atZone(UTC);
        int numberOfDeletedPayments = paymentDao.deletePayments(maxDate, expungeHistoricalDataConfig.getNumberOfTransactionsToExpunge());
        LOGGER.info(format("%s payments were deleted.", numberOfDeletedPayments));
    }
}
