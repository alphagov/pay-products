package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.client.publicapi.model.PaymentState;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.PaymentStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(MockitoJUnitRunner.class)
public class PaymentFinderTest {

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private PublicApiRestClient publicApiRestClient;

    private PaymentFinder paymentFinder;
    private LinksDecorator linksDecorator;

    @Before
    public void setup() throws Exception {
        linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay");
        paymentFinder = new PaymentFinder(paymentDao, linksDecorator, publicApiRestClient);
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

    @Test
    public void shouldReturnPaymentWithExternalStatus_whenFoundByExternalIdAndStatusIsSubmitted() throws Exception {
        String externalId = randomUuid();
        PaymentEntity paymentEntity = new PaymentEntity();
        ProductEntity productEntity = ProductEntityFixture.aProductEntity().build();
        paymentEntity.setExternalId(externalId);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(PaymentStatus.SUBMITTED);
        paymentEntity.setGovukPaymentId(externalId);
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setState(new PaymentState("submitted", true, "a message", "a code"));
        when(paymentDao.findByExternalId(externalId)).thenReturn(Optional.of(paymentEntity));
        when(publicApiRestClient.getPayment(productEntity.getPayApiToken(), paymentEntity.getGovukPaymentId()))
                .thenReturn(Optional.of(paymentResponse));

        Optional<Payment> optionalPayment = paymentFinder.findByExternalId(externalId);
        assertThat(optionalPayment.isPresent(), is(true));

        Payment payment = optionalPayment.get();
        assertThat(payment.getGovUkStatus(), is("submitted"));
    }

    @Test
    public void shouldReturnPaymentWithNoExternalStatus_whenFoundByExternalIdAndStatusIsCreated() throws Exception {
        String externalId = randomUuid();
        PaymentEntity paymentEntity = new PaymentEntity();
        ProductEntity productEntity = ProductEntityFixture.aProductEntity().build();
        paymentEntity.setExternalId(externalId);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(PaymentStatus.CREATED);
        paymentEntity.setGovukPaymentId(externalId);
        when(paymentDao.findByExternalId(externalId)).thenReturn(Optional.of(paymentEntity));

        Optional<Payment> optionalPayment = paymentFinder.findByExternalId(externalId);
        assertThat(optionalPayment.isPresent(), is(true));

        Payment payment = optionalPayment.get();
        assertThat(payment.getGovUkStatus(), is(nullValue()));
    }

    @Test
    public void shouldReturnPaymentWithNoExternalStatus_whenFoundByExternalIdAndStatusIsError() throws Exception {
        String externalId = randomUuid();
        PaymentEntity paymentEntity = new PaymentEntity();
        ProductEntity productEntity = ProductEntityFixture.aProductEntity().build();
        paymentEntity.setExternalId(externalId);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(PaymentStatus.ERROR);
        paymentEntity.setGovukPaymentId(externalId);
        when(paymentDao.findByExternalId(externalId)).thenReturn(Optional.of(paymentEntity));

        Optional<Payment> optionalPayment = paymentFinder.findByExternalId(externalId);
        assertThat(optionalPayment.isPresent(), is(true));

        Payment payment = optionalPayment.get();
        assertThat(payment.getGovUkStatus(), is(nullValue()));
    }
}
