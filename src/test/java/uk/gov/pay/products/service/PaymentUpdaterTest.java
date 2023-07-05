package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.fixtures.PaymentEntityFixture.aPaymentEntity;
import static uk.gov.pay.products.service.PaymentUpdater.REDACTED_REFERENCE_NUMBER;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class PaymentUpdaterTest {

    @Mock
    private PaymentDao paymentDao;

    @Captor
    private ArgumentCaptor<PaymentEntity> paymentEntityArgumentCaptor;
    
    private PaymentUpdater paymentUpdater;

    @Before
    public void setup() {
        paymentUpdater = new PaymentUpdater(paymentDao);
    }
    
    @Test
    public void shouldRedactPaymentReference() {
        String govukPaymentId = randomUuid();
        PaymentEntity paymentEntity = aPaymentEntity()
                .withReferenceNumber("referenceNumber")
                .withGovukPaymentId(govukPaymentId)
                .build();
        when(paymentDao.findByGovukPaymentId(govukPaymentId)).thenReturn(java.util.Optional.of(paymentEntity));

        paymentUpdater.redactReference(govukPaymentId);
        
        verify(paymentDao).merge(paymentEntityArgumentCaptor.capture());
        assertThat(paymentEntityArgumentCaptor.getValue().getReferenceNumber(), is(REDACTED_REFERENCE_NUMBER));
    }

    @Test
    public void shouldRedactPaymentReferenceByExternalId() {
        String externalId = "external-id";
        PaymentEntity paymentEntity = aPaymentEntity()
                .withReferenceNumber("referenceNumber")
                .withExternalId("external-id")
                .withGovukPaymentId(externalId)
                .build();
        when(paymentDao.findByExternalId(externalId)).thenReturn(Optional.of(paymentEntity));

        paymentUpdater.redactReferenceByExternalId(externalId);

        verify(paymentDao).merge(paymentEntityArgumentCaptor.capture());
        assertThat(paymentEntityArgumentCaptor.getValue().getReferenceNumber(), is(REDACTED_REFERENCE_NUMBER));
    }
}
