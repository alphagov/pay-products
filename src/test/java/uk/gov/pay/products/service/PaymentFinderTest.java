package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class PaymentFinderTest {

    @Mock
    private PaymentDao paymentDao;

    private PaymentFinder paymentFinder;
    private LinksDecorator linksDecorator;

    @Before
    public void setup() throws Exception {
        linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay");
        paymentFinder = new PaymentFinder(paymentDao, linksDecorator);
    }

    @Test
    public void shouldReturnPayment_whenFoundByExternalId() throws Exception{
        String externalId = randomUuid();
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setExternalId(externalId);
        when(paymentDao.findByExternalId(externalId)).thenReturn(Optional.of(paymentEntity));

        Optional<Payment> paymentOptional = paymentFinder.findByExternalId(externalId);

        assertThat(paymentOptional.isPresent(), is(true));
        assertThat(paymentOptional.get().getExternalId(), is(externalId));
    }

    @Test
    public void shouldReturnEmpty_whenNoPaymentFound() throws Exception {
        String externalId = randomUuid();
        when(paymentFinder.findByExternalId(externalId)).thenReturn(Optional.empty());

        Optional<Payment> paymentOptional = paymentFinder.findByExternalId(externalId);

        assertThat(paymentOptional.isPresent(), is(false));
    }

    @Test
    public void shouldReturnAList_whenFoundByProductExternalId() throws Exception{
        String externalPaymentId = randomUuid();
        String externalProductId = randomUuid();
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setExternalId(externalPaymentId);
        List<PaymentEntity> paymentList = Arrays.asList(paymentEntity);

       when(paymentDao.findByProductExternalId(externalProductId)).thenReturn(paymentList);

        List<Payment> expectedPaymentList = paymentFinder.findByProductExternalId(externalProductId);

        assertThat(expectedPaymentList.isEmpty(), is(false));
        assertThat(expectedPaymentList.get(0).getExternalId(), is(externalPaymentId));
        assertThat(expectedPaymentList.size(), is(1));
    }
}
