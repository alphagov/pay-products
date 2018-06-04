package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.pay.products.client.publicapi.PaymentRequest;
import uk.gov.pay.products.client.publicapi.PaymentResponse;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.client.publicapi.model.Link;
import uk.gov.pay.products.client.publicapi.model.Links;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.exception.BadPaymentRequestException;
import uk.gov.pay.products.exception.PaymentCreationException;
import uk.gov.pay.products.exception.PaymentCreatorNotFoundException;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.matchers.PaymentEntityMatcher;
import uk.gov.pay.products.matchers.PaymentRequestMatcher;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.dao.PaymentDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.service.transaction.TransactionFlow;
import uk.gov.pay.products.util.PaymentStatus;
import uk.gov.pay.products.util.RandomIdGenerator;

import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.pay.products.util.PaymentStatus.ERROR;
import static uk.gov.pay.products.util.PaymentStatus.SUBMITTED;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUserFriendlyReference;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RandomIdGenerator.class)
public class PaymentCreatorTest {
    static private String PRODUCT_URL = "https://products.url";
    static private String PRODUCT_UI_URL = "https://products-ui.url";
    static private String FRIENDLY_URL = "https://products-ui.url/payments";

    @Mock
    private ProductDao productDao;

    @Mock
    private PaymentDao paymentDao;

    @Mock
    private PublicApiRestClient publicApiRestClient;

    @Mock
    private ProductsConfiguration productsConfiguration;

    private PaymentCreator paymentCreator;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() throws Exception {
        LinksDecorator linksDecorator = new LinksDecorator(PRODUCT_URL, PRODUCT_UI_URL, FRIENDLY_URL);
        paymentCreator = new PaymentCreator(TransactionFlow::new, productDao, paymentDao, publicApiRestClient, linksDecorator, productsConfiguration);
    }

    @Test
    public void shouldCreateASuccessfulPayment_whenReturnUrlIsPresent() throws Exception {
        PowerMockito.mockStatic(RandomIdGenerator.class);
        int productId = 1;
        String productExternalId = "product-external-id";
        String paymentExernalId = "payment-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";
        String referenceNumber = createRandomReferenceNumber();

        String paymentId = "payment-id";
        Long paymentAmount = 50L;
        String paymentNextUrl = "http://next.url";
        String productsUIConfirmUri = "https://products-ui/payment-complete";
        String paymentReturnUrl = format("%s/%s", productsUIConfirmUri, paymentExernalId);

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                productReturnUrl,
                productApiToken,
                false);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                productPrice,
                referenceNumber,
                productName,
                paymentReturnUrl);
        PaymentResponse paymentResponse = createPaymentResponse(
                paymentId,
                paymentAmount,
                paymentNextUrl,
                productReturnUrl);


        when(randomUuid()).thenReturn(paymentExernalId);
        when(randomUserFriendlyReference()).thenReturn(referenceNumber);
        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(publicApiRestClient.createPayment(argThat(is(productApiToken)), argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest)))).thenReturn(paymentResponse);
        when(productsConfiguration.getProductsUiConfirmUrl()).thenReturn(productsUIConfirmUri);

        Payment payment = paymentCreator.doCreate(productExternalId, null, null);

        assertNotNull(payment);
        assertNotNull(payment.getExternalId());
        assertThat(payment.getGovukPaymentId(), is(paymentResponse.getPaymentId()));
        assertThat(payment.getNextUrl(), is(paymentResponse.getLinks().getNextUrl().getHref()));
        assertThat(payment.getAmount(), is(paymentResponse.getAmount()));
        assertNotNull(payment.getLinks());
        assertThat(payment.getLinks().size(), is(2));
        assertThat(payment.getLinks().get(0).getMethod(), is("GET"));
        assertThat(payment.getLinks().get(0).getHref(), is(PRODUCT_URL + "/v1/api/payments/" + payment.getExternalId()));
        assertThat(payment.getLinks().get(1).getMethod(), is("GET"));
        assertThat(payment.getLinks().get(1).getHref(), is(paymentResponse.getLinks().getNextUrl().getHref()));
        assertThat(payment.getProductId(), is(productEntity.getId()));
        assertThat(payment.getProductExternalId(), is(productEntity.getExternalId()));
        assertThat(payment.getStatus(), is(SUBMITTED));

        PaymentEntity expectedPaymentEntity = createPaymentEntity(
                paymentId,
                paymentNextUrl,
                productEntity,
                SUBMITTED,
                paymentAmount);
        verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
    }

    @Test
    public void shouldCreateASuccessfulPayment_whenReturnUrlIsNotPresent() throws Exception {
        PowerMockito.mockStatic(RandomIdGenerator.class);

        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";

        String paymentId = "payment-id";
        String paymentExternalId = "random-external-id";
        Long paymentAmount = 50L;
        String paymentNextUrl = "http://next.url";
        String referenceNumber = createRandomReferenceNumber();

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                "",
                productApiToken,
                false);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                productPrice,
                referenceNumber,
                productName,
                productReturnUrl + "/" + paymentExternalId);
        PaymentResponse paymentResponse = createPaymentResponse(
                paymentId,
                paymentAmount,
                paymentNextUrl,
                productReturnUrl);


        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(randomUuid()).thenReturn(paymentExternalId);
        when(randomUserFriendlyReference()).thenReturn(referenceNumber);
        when(productsConfiguration.getProductsUiConfirmUrl()).thenReturn(productReturnUrl);
        when(publicApiRestClient.createPayment(argThat(is(productApiToken)), argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest)))).thenReturn(paymentResponse);

        Payment payment = paymentCreator.doCreate(productExternalId, null, null);

        assertNotNull(payment);
        assertNotNull(payment.getExternalId());
        assertThat(payment.getGovukPaymentId(), is(paymentResponse.getPaymentId()));
        assertThat(payment.getNextUrl(), is(paymentResponse.getLinks().getNextUrl().getHref()));
        assertThat(payment.getAmount(), is(paymentResponse.getAmount()));
        assertNotNull(payment.getLinks());
        assertThat(payment.getLinks().size(), is(2));
        assertThat(payment.getLinks().get(0).getMethod(), is("GET"));
        assertThat(payment.getLinks().get(0).getHref(), is(PRODUCT_URL + "/v1/api/payments/" + payment.getExternalId()));
        assertThat(payment.getLinks().get(1).getMethod(), is("GET"));
        assertThat(payment.getLinks().get(1).getHref(), is(paymentResponse.getLinks().getNextUrl().getHref()));
        assertThat(payment.getProductId(), is(productEntity.getId()));
        assertThat(payment.getProductExternalId(), is(productEntity.getExternalId()));
        assertThat(payment.getStatus(), is(SUBMITTED));

        PaymentEntity expectedPaymentEntity = createPaymentEntity(
                paymentId,
                paymentNextUrl,
                productEntity,
                SUBMITTED,
                paymentAmount);
        verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
    }

    @Test
    public void shouldCreateASuccessfulPayment_withUserDefinedReference_whenReferencePresent() throws Exception {
        PowerMockito.mockStatic(RandomIdGenerator.class);
        
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";
        String userDefinedReference = "user-defined-reference";

        String paymentId = "payment-id";
        String paymentExternalId = "random-external-id";
        String paymentNextUrl = "http://next.url";

        Long priceOverride = 500L;

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                "",
                productApiToken,
                true);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                priceOverride,
                userDefinedReference,
                productName,
                productReturnUrl + "/" + paymentExternalId);
        PaymentResponse paymentResponse = createPaymentResponse(
                paymentId,
                priceOverride,
                paymentNextUrl,
                productReturnUrl);


        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(randomUuid()).thenReturn(paymentExternalId);
        when(productsConfiguration.getProductsUiConfirmUrl()).thenReturn(productReturnUrl);
        when(publicApiRestClient.createPayment(argThat(is(productApiToken)), argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest)))).thenReturn(paymentResponse);

        Payment payment = paymentCreator.doCreate(productExternalId, priceOverride, userDefinedReference);

        assertNotNull(payment);
        assertNotNull(payment.getExternalId());
        assertThat(payment.getGovukPaymentId(), is(paymentResponse.getPaymentId()));
        assertThat(payment.getAmount(), is(500L));
        assertThat(payment.getReferenceNumber(), is(userDefinedReference));

        PaymentEntity expectedPaymentEntity = createPaymentEntity(
                paymentId,
                userDefinedReference,
                paymentNextUrl,
                productEntity,
                SUBMITTED,
                priceOverride);
        verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
    }

    @Test
    public void shouldCreateASuccessfulPayment_withOverridePrice_whenPriceOverridePresent() throws Exception {
        PowerMockito.mockStatic(RandomIdGenerator.class);
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";

        String paymentId = "payment-id";
        String paymentExternalId = "random-external-id";
        String paymentNextUrl = "http://next.url";
        String referenceNumber = createRandomReferenceNumber();

        Long priceOverride = 500L;

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                "",
                productApiToken,
                false);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                priceOverride,
                referenceNumber,
                productName,
                productReturnUrl + "/" + paymentExternalId);
        PaymentResponse paymentResponse = createPaymentResponse(
                paymentId,
                priceOverride,
                paymentNextUrl,
                productReturnUrl);


        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(randomUuid()).thenReturn(paymentExternalId);
        when(randomUserFriendlyReference()).thenReturn(referenceNumber);
        when(productsConfiguration.getProductsUiConfirmUrl()).thenReturn(productReturnUrl);
        when(publicApiRestClient.createPayment(argThat(is(productApiToken)), argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest)))).thenReturn(paymentResponse);

        Payment payment = paymentCreator.doCreate(productExternalId, priceOverride, null);

        assertNotNull(payment);
        assertNotNull(payment.getExternalId());
        assertThat(payment.getGovukPaymentId(), is(paymentResponse.getPaymentId()));
        assertThat(payment.getAmount(), is(500L));

        PaymentEntity expectedPaymentEntity = createPaymentEntity(
                paymentId,
                paymentNextUrl,
                productEntity,
                SUBMITTED,
                priceOverride);
        verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
    }

    @Test
    public void shouldCreateAnErrorPayment_whenPublicApiCallFails() throws Exception {
        PowerMockito.mockStatic(RandomIdGenerator.class);

        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";

        String paymentExternalId = "random-external-id";
        String referenceNumber = createRandomReferenceNumber();
        String productsUIConfirmUri = "https://products-ui/payment-complete";
        String paymentReturnUrl = format("%s/%s", productsUIConfirmUri, paymentExternalId);


        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                productReturnUrl,
                productApiToken,
                false);
        PaymentRequest expectedPaymentRequest = createPaymentRequest(
                productPrice,
                referenceNumber,
                productName,
                paymentReturnUrl);

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(randomUuid()).thenReturn(paymentExternalId);
        when(randomUserFriendlyReference()).thenReturn(referenceNumber);
        when(productsConfiguration.getProductsUiConfirmUrl()).thenReturn(productsUIConfirmUri);
        when(publicApiRestClient.createPayment(argThat(is(productApiToken)), argThat(PaymentRequestMatcher.isSame(expectedPaymentRequest))))
                .thenThrow(PublicApiResponseErrorException.class);

        try {
            paymentCreator.doCreate(productExternalId, null, null);
            fail("Expected an PaymentCreationException to be thrown");
        } catch (PaymentCreationException e) {
            assertThat(e.getProductExternalId(), is(productExternalId));
            PaymentEntity expectedPaymentEntity = createPaymentEntity(
                    null,
                    null,
                    productEntity,
                    ERROR,
                    null);
            verify(paymentDao).merge(argThat(PaymentEntityMatcher.isSame(expectedPaymentEntity)));
        }
    }

    @Test
    public void shouldThrowPaymentCreatorNotFoundException_whenProductIsNotFound() throws Exception {
        String productExternalId = "product-external-id";

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.empty());

        try {
            paymentCreator.doCreate(productExternalId, null, null);
            fail("Expected an PaymentCreatorNotFoundException to be thrown");
        } catch (PaymentCreatorNotFoundException e) {
            assertThat(e.getProductExternalId(), is(productExternalId));
        }

    }
    
    @Test
    public void shouldThrowPaymentCreationException_whenReferenceEnabledAndNoReferencePresent() {
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productReturnUrl = "https://return.url";
        String productApiToken = "api-token";

        Integer gatewayAccountId = 1;

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                productReturnUrl,
                productApiToken,
                true);

        PaymentRequest paymentRequest = createPaymentRequest(
                productPrice,
                null,
                productName,
                "https://return.url");

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));

        thrown.expect(BadPaymentRequestException.class);
        thrown.expectMessage("User defined reference is enabled but missing");
        paymentCreator.doCreate(productExternalId, null, null);
    }

    @Test
    public void shouldThrowRuntimeException_whenTooManyConflictsInReferenceNumbers() {
        int productId = 1;
        String productExternalId = "product-external-id";
        long productPrice = 100L;
        String productName = "name";
        String productApiToken = "api-token";

        Integer gatewayAccountId = 1;

        ProductEntity productEntity = createProductEntity(
                productId,
                productPrice,
                productExternalId,
                productName,
                "",
                productApiToken,
                gatewayAccountId);

        when(productDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        doThrow(new javax.persistence.RollbackException("payments_gateway_account_id_reference_number_key duplicate key value violates unique constraint"))
                .when(paymentDao).persist(any(PaymentEntity.class));

        Exception exception = null;
        try {
            paymentCreator.doCreate(productExternalId, null, null);
        } catch (RuntimeException ex) {
            exception = ex;
        }
        assertThat(isNull(exception), is(false));
        assertThat(exception instanceof RuntimeException, is(true));
        assertThat(exception.getMessage().contains("Too many conflicts generating unique user friendly reference numbers for gateway account"), is(true));
        verify(paymentDao, times(3)).persist(any(PaymentEntity.class));
    }
    
    private ProductEntity createProductEntity(int id, long price, String externalId, String name, String returnUrl, String apiToken, Integer gatewayAccountId) {
        ProductEntity productEntity = createProductEntity(id, price, externalId, name, returnUrl, apiToken, false);
        productEntity.setGatewayAccountId(gatewayAccountId);

        return productEntity;
    }

    private ProductEntity createProductEntity(int id, long price, String externalId, String name, String returnUrl, String apiToken, Boolean referenceEnabled) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(id);
        productEntity.setPrice(price);
        productEntity.setExternalId(externalId);
        productEntity.setName(name);
        productEntity.setReturnUrl(returnUrl);
        productEntity.setPayApiToken(apiToken);
        productEntity.setReferenceEnabled(referenceEnabled);

        return productEntity;
    }

    private PaymentResponse createPaymentResponse(String id, Long amount, String link, String returnUrl) {
        Links links = new Links();
        links.setNextUrl(new Link(link, "GET", "multipart/form-data"));
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setPaymentId(id);
        paymentResponse.setAmount(amount);
        paymentResponse.setLinks(links);
        paymentResponse.setReturnUrl(returnUrl);

        return paymentResponse;
    }

    private PaymentRequest createPaymentRequest(long price, String externalId, String description, String returnUrl) {
        return new PaymentRequest(price, externalId, description, returnUrl);
    }

    private PaymentEntity createPaymentEntity(String paymentId, String nextUrl, ProductEntity productEntity, PaymentStatus status, Long amount) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setGovukPaymentId(paymentId);
        paymentEntity.setNextUrl(nextUrl);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(status);
        paymentEntity.setAmount(amount);
        return paymentEntity;
    }

    private PaymentEntity createPaymentEntity(String paymentId, String referenceNumber, String nextUrl, ProductEntity productEntity, PaymentStatus status, Long amount) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setGovukPaymentId(paymentId);
        paymentEntity.setReferenceNumber(referenceNumber);
        paymentEntity.setNextUrl(nextUrl);
        paymentEntity.setProductEntity(productEntity);
        paymentEntity.setStatus(status);
        paymentEntity.setAmount(amount);
        return paymentEntity;
    }

    private String createRandomReferenceNumber() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(1, 10);
    }
}
