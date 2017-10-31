package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.client.publicapi.PaymentRequest;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.exception.PaymentCreatorDownstreamException;
import uk.gov.pay.products.exception.PaymentCreatorNotFoundException;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.client.publicapi.model.Link;
import uk.gov.pay.products.client.publicapi.model.Links;
import uk.gov.pay.products.matchers.PaymentEntityMatcher;
import uk.gov.pay.products.matchers.PaymentRequestMatcher;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.service.transaction.TransactionFlow;
import uk.gov.pay.products.util.PaymentStatus;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.pay.products.util.PaymentStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCreatorTest {

    @Mock
    private ProductDao productDao;

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private PublicApiRestClient publicApiRestClient;

    private PaymentCreator paymentCreator;

    @Before
    public void setup() throws Exception {
        LinksDecorator linksDecorator = new LinksDecorator("https://products.url", "https://products-ui.url");
        paymentCreator = new PaymentCreator(TransactionFlow::new, productDao, paymentDao, publicApiRestClient, linksDecorator);
    }

    @Test
    public void shouldCreateASuccessfulPayment() throws Exception {
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productDescription = "description";
        String productReturnUrl = "https://return.url";

        String paymentId = "payment-id";
        String paymentNextUrl = "http://next.url";

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productDescription,
                productReturnUrl);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                productPrice,
                productExternalId,
                productDescription,
                productReturnUrl);
        PaymentResponse paymentResponse = createPaymentResponse(
                paymentId,
                paymentNextUrl);


        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(publicApiRestClient.createPayment(argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest)))).thenReturn(paymentResponse);

        Payment payment = paymentCreator.doCreate(productExternalId);

        assertNotNull(payment);
        assertNotNull(payment.getExternalId());
        assertThat(payment.getGovukPaymentId(), is(paymentResponse.getPaymentId()));
        assertThat(payment.getNextUrl(), is(paymentResponse.getLinks().getNextUrl().getHref()));
        assertThat(payment.getProductId(), is(productEntity.getId()));
        assertThat(payment.getProductExternalId(), is(productEntity.getExternalId()));
        assertThat(payment.getStatus(), is(SUCCESS));

        PaymentEntity expectedPaymentEntity = createPaymentEntity(
                paymentId,
                paymentNextUrl,
                productEntity,
                SUCCESS);
        verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
    }

    @Test
    public void shouldCreateAnErrorPayment_whenPublicApiCallFails() throws Exception {
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productDescription = "description";
        String productReturnUrl = "https://return.url";

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productDescription,
                productReturnUrl);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                productPrice,
                productExternalId,
                productDescription,
                productReturnUrl);

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(publicApiRestClient.createPayment(argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest))))
                .thenThrow(PublicApiResponseErrorException.class);

        try {
            paymentCreator.doCreate(productExternalId);
            fail("Expected an PaymentCreatorDownstreamException to be thrown");
        } catch (PaymentCreatorDownstreamException e) {
            assertThat(e.getProductExternalId(), is(productExternalId));
            PaymentEntity expectedPaymentEntity = createPaymentEntity(
                    null,
                    null,
                    productEntity,
                    ERROR);
            verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
        }
    }

    @Test
    public void shouldThrowPaymentCreatorNotFoundException_whenProductIsNotFound() throws Exception {
        String productExternalId = "product-external-id";

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.empty());

        try {
            paymentCreator.doCreate(productExternalId);
            fail("Expected an PaymentCreatorNotFoundException to be thrown");
        } catch (PaymentCreatorNotFoundException e) {
            assertThat(e.getProductExternalId(), is(productExternalId));
        }

    }


    private ProductEntity createProductEntity(int id, long price, String externalId, String description, String returnUrl) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(id);
        productEntity.setPrice(price);
        productEntity.setExternalId(externalId);
        productEntity.setDescription(description);
        productEntity.setReturnUrl(returnUrl);

        return productEntity;
    }

    private PaymentResponse createPaymentResponse(String id, String link) {
        Links links = new Links();
        links.setNextUrl(new Link(link, "GET", "multipart/form-data"));
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(id);
        paymentResponse.setLinks(links);

        return paymentResponse;
    }

    private PaymentRequest createPaymentRequest(long price, String externalId, String description, String returnUrl) {
        return new PaymentRequest(price, externalId, description, returnUrl);
    }

    private PaymentEntity createPaymentEntity(String paymentId, String nextUrl, ProductEntity productEntity, PaymentStatus status) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setGovukPaymentId(paymentId);
        paymentEntity.setNextUrl(nextUrl);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(status);
        return paymentEntity;
    }
}
