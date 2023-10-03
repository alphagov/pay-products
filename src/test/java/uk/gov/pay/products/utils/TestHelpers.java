package uk.gov.pay.products.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.mockito.ArgumentCaptor;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;
import uk.gov.pay.products.util.RandomIdGenerator;

import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class TestHelpers {

    public static PaymentEntity createPaymentEntity(ProductEntity productEntity, ZonedDateTime date, int daysToMinusFromDate) {
        return PaymentEntityFixture.aPaymentEntity()
                .withReferenceNumber(RandomIdGenerator.randomUserFriendlyReference())
                .withProduct(productEntity)
                .withGatewayAccountId(randomInt())
                .withDateCreated(date.minusDays(daysToMinusFromDate))
                .build();
    }

    public static PaymentEntity createPaymentEntity(ProductEntity productEntity, PaymentStatus paymentStatus, String referenceNumber, Integer gatewayAccountId) {
        return PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(paymentStatus)
                .withProduct(productEntity)
                .withReferenceNumber(referenceNumber)
                .withGatewayAccountId(gatewayAccountId)
                .build();
    }

    public static PaymentEntity createPaymentEntity(ProductEntity productEntity, PaymentStatus paymentStatus, String referenceNumber, String govukPaymentId) {
        return PaymentEntityFixture.aPaymentEntity()
                .withGovukPaymentId(govukPaymentId)
                .withStatus(paymentStatus)
                .withProduct(productEntity)
                .withReferenceNumber(referenceNumber)
                .build();
    }

    public static void verifyLog(
            final Appender<ILoggingEvent> mockAppender,
            final ArgumentCaptor<LoggingEvent> captorLoggingEvent,
            final int expectedNumOfInvocations,
            final String expectedLogMessage) {
        verify(mockAppender, times(expectedNumOfInvocations)).doAppend(captorLoggingEvent.capture());
        final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
        assertThat(loggingEvent.getLevel(), is(Level.INFO));
        assertThat(loggingEvent.getFormattedMessage(), is(expectedLogMessage));
    }
}
