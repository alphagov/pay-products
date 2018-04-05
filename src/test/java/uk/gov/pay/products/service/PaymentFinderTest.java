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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.utils.RandomIdGenerator.randomUuid;

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
        linksDecorator = new LinksDecorator("http://localhost", "http://localhost/pay", "http://localhost/payments");
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
        String productExternalId = randomUuid();
        String paymentExternalId_1 = randomUuid();
        String paymentExternalId_2 = randomUuid();
        PaymentEntity paymentEntity_1 = new PaymentEntity();
        PaymentEntity paymentEntity_2 = new PaymentEntity();
        ProductEntity productEntity = ProductEntityFixture.aProductEntity().build();

        paymentEntity_1.setExternalId(paymentExternalId_1);
        paymentEntity_1.setProductEntity(productEntity);
        paymentEntity_1.setStatus(PaymentStatus.SUBMITTED);
        paymentEntity_1.setGovukPaymentId(randomUuid());

        paymentEntity_2.setExternalId(paymentExternalId_2);
        paymentEntity_2.setProductEntity(productEntity);
        paymentEntity_2.setStatus(PaymentStatus.CREATED);
        paymentEntity_2.setGovukPaymentId(randomUuid());

        List<PaymentEntity> paymentList = Arrays.asList(paymentEntity_1, paymentEntity_2);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setState(new PaymentState("success", true, "a message", "a code"));

        when(paymentDao.findByProductExternalId(productExternalId)).thenReturn(paymentList);
        when(publicApiRestClient.getPayment(productEntity.getPayApiToken(), paymentEntity_1.getGovukPaymentId()))
                .thenReturn(Optional.of(paymentResponse));

        List<Payment> expectedPaymentList = paymentFinder.findByProductExternalId(productExternalId);

        assertThat(expectedPaymentList.isEmpty(), is(false));
        assertThat(expectedPaymentList.size(), is(2));

        Optional<Payment> payment_1 = expectedPaymentList.stream().filter(payment -> payment.getExternalId().equals(paymentExternalId_1)).findFirst();
        assertThat(payment_1.isPresent(), is(true));
        assertThat(payment_1.get().getGovUkStatus(), is("success"));

        Optional<Payment> payment_2 = expectedPaymentList.stream().filter(payment -> payment.getExternalId().equals(paymentExternalId_2)).findFirst();
        assertThat(payment_2.isPresent(), is(true));
        assertThat(payment_2.get().getGovUkStatus(), is(nullValue()));
    }

    @Test
    public void shouldReturnAnEmptyList_whenThereQueriedByProductExternalIdAndThereAreNoCorrespondingPayments() throws Exception {
        String productExternalId = randomUuid();
        List<PaymentEntity> paymentList = new ArrayList<>();
        when(paymentDao.findByProductExternalId(productExternalId)).thenReturn(paymentList);

        List<Payment> expectedPaymentList = paymentFinder.findByProductExternalId(productExternalId);

        assertThat(expectedPaymentList.isEmpty(), is(true));
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
