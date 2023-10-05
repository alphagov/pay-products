package uk.gov.pay.products.service;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.config.ExpungeHistoricalDataConfig;
import uk.gov.pay.products.persistence.dao.PaymentDao;

import javax.inject.Inject;
import java.time.Clock;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static net.logstash.logback.argument.StructuredArguments.kv;

public class PaymentDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentDeleter.class);
    
    private ExpungeHistoricalDataConfig expungeHistoricalDataConfig;
    private PaymentDao paymentDao;
    private Clock clock;

    private static final Counter noOfPaymentsDeletedMetric = Counter.build()
            .name("expunge_historical_data_job_no_of_payments_deleted")
            .help("Number of payments deleted")
            .register();

    private static final Histogram duration = Histogram.build()
            .name("expunge_historical_data_job_duration_seconds")
            .help("Duration of expunge historical data job in seconds")
            .unit("seconds")
            .register();

    @Inject
    public PaymentDeleter(ExpungeHistoricalDataConfig expungeHistoricalDataConfig, PaymentDao paymentDao, Clock clock) {
        this.expungeHistoricalDataConfig = expungeHistoricalDataConfig;
        this.paymentDao = paymentDao;
        this.clock = clock;
    }

    public void deletePayments() {
        Histogram.Timer responseTimeTimer = duration.startTimer();
        
        try {
            if (!expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()) {
                LOGGER.info("Expunging of historical data is not enabled.");
                return;
            }

            var maxDate = clock.instant().minus(expungeHistoricalDataConfig.getExpungeDataOlderThanDays(), ChronoUnit.DAYS).atZone(UTC);
            int numberOfDeletedPayments = paymentDao.deletePayments(maxDate, expungeHistoricalDataConfig.getNumberOfPaymentsToExpunge());
            LOGGER.info(format("%s payments were deleted.", numberOfDeletedPayments),
                    kv("no_of_payments_deleted", numberOfDeletedPayments));

            noOfPaymentsDeletedMetric.inc(numberOfDeletedPayments);
        } finally {
            responseTimeTimer.observeDuration();
        }
    }
}
