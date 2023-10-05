package uk.gov.pay.products.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.config.ExpungeHistoricalDataConfig;
import uk.gov.pay.products.persistence.dao.PaymentDao;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.utils.TestHelpers.verifyLog;

@RunWith(MockitoJUnitRunner.class)
public class PaymentDeleterTest {

    private PaymentDeleter paymentDeleter;
    
    @Mock
    private ExpungeHistoricalDataConfig expungeHistoricalDataConfig;
    
    @Mock
    private PaymentDao paymentDao;

    private final Clock clock = Clock.fixed(Instant.parse("2022-03-03T10:15:30Z"), UTC);

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    private final CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;

    @Before
    public void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
        logger.setLevel(Level.INFO);
        
        paymentDeleter = new PaymentDeleter(expungeHistoricalDataConfig, paymentDao, clock);
    }
    
    @Test
    public void shouldRecordMetrics() {
        Double initialDuration = Optional.ofNullable(collectorRegistry.getSampleValue("expunge_historical_data_job_duration_seconds_sum")).orElse(0.0);
        Double initialNoOfPaymentsDeletedMetric = Optional.ofNullable(collectorRegistry.getSampleValue("expunge_historical_data_job_no_of_payments_deleted_total")).orElse(0.0);

        shouldDeletePayments();
        
        Double duration = collectorRegistry.getSampleValue("expunge_historical_data_job_duration_seconds_sum");
        assertThat(duration, greaterThan(initialDuration));

        Double noOfTxsRedactedMetric = collectorRegistry.getSampleValue("expunge_historical_data_job_no_of_payments_deleted_total");
        assertThat(noOfTxsRedactedMetric, is(initialNoOfPaymentsDeletedMetric + 3));
    }
    
    @Test
    public void shouldDeletePayments() {
        when(expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()).thenReturn(true);
        when(expungeHistoricalDataConfig.getExpungeDataOlderThanDays()).thenReturn(1);
        when(expungeHistoricalDataConfig.getNumberOfPaymentsToExpunge()).thenReturn(3);

        when(paymentDao.deletePayments(any(), anyInt())).thenReturn(3);
        
        paymentDeleter.deletePayments();
        
        verify(paymentDao, times(1)).deletePayments(ZonedDateTime.now(clock).minusDays(1), 3);
    }
    
    @Test
    public void shouldNotDeletePaymentsIfNotEnabled() {
        Double initialNoOfPaymentsDeletedMetric = Optional.ofNullable(collectorRegistry.getSampleValue("expunge_historical_data_job_no_of_payments_deleted_total")).orElse(0.0);
        
        when(expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()).thenReturn(false);
        paymentDeleter.deletePayments();
        verifyNoMoreInteractions(paymentDao);
        verifyLog(mockAppender, loggingEventArgumentCaptor, 1,"Expunging of historical data is not enabled.");

        Double noOfTxsRedactedMetric = collectorRegistry.getSampleValue("expunge_historical_data_job_no_of_payments_deleted_total");
        assertThat(noOfTxsRedactedMetric, is(initialNoOfPaymentsDeletedMetric));
    }
}
