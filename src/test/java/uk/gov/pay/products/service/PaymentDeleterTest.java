package uk.gov.pay.products.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.config.ExpungeHistoricalDataConfig;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.utils.TestHelpers.createPaymentEntity;
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

    @Before
    public void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
        logger.setLevel(Level.INFO);
        
        paymentDeleter = new PaymentDeleter(expungeHistoricalDataConfig, paymentDao, clock);
    }
    
    @Test
    public void shouldDeletePayments() {
        when(expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()).thenReturn(true);
        when(expungeHistoricalDataConfig.getExpungeDataOlderThanDays()).thenReturn(1);
        when(expungeHistoricalDataConfig.getNumberOfTransactionsToExpunge()).thenReturn(3);

        var payment1 = createPaymentEntity(ProductEntityFixture.aProductEntity().build(), ZonedDateTime.now(clock), 2);
        var payment2 = createPaymentEntity(ProductEntityFixture.aProductEntity().build(), ZonedDateTime.now(clock), 3);
        var payment3 = createPaymentEntity(ProductEntityFixture.aProductEntity().build(), ZonedDateTime.now(clock), 4);
        List<PaymentEntity> paymentsToDelete = List.of(payment1, payment2, payment3);
        when(paymentDao.getPaymentsForDeletion(any(), anyInt())).thenReturn(paymentsToDelete);
        when(paymentDao.deletePayments(any())).thenReturn(3);
        
        paymentDeleter.deletePayments();
        
        verify(paymentDao, times(1)).getPaymentsForDeletion(ZonedDateTime.now(clock).minusDays(1), 3);
        verify(paymentDao, times(1)).deletePayments(paymentsToDelete.stream().map(PaymentEntity::getExternalId).collect(toList()));
    }
    
    @Test
    public void shouldNotDeletePaymentsIfNotEnabled() {
        when(expungeHistoricalDataConfig.isExpungeHistoricalDataEnabled()).thenReturn(false);
        paymentDeleter.deletePayments();
        verifyNoMoreInteractions(paymentDao);
        verifyLog(mockAppender, loggingEventArgumentCaptor, 1,
                "Expunging of historical data is not enabled.");
    }
}
