package uk.gov.pay.products.client.publicapi;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.testing.port.PortFactory;
import uk.gov.pay.products.client.RestClientFactory;
import uk.gov.pay.products.config.RestClientConfiguration;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.stubs.publicapi.PublicApiStub;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.model.Source.CARD_PAYMENT_LINK;
import static uk.gov.pay.products.matchers.PaymentResponseMatcher.hasAllPaymentProperties;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.createPaymentRequestPayload;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.setupResponseToCreatePaymentRequest;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.setupResponseToGetPaymentRequest;

public class PublicApiRestClientTest {
    private final static int PUBLIC_API_PORT = PortFactory.findFreePort();
    private static final Duration FIFTY_SECONDS = Duration.seconds(50);
    
    @ClassRule
    public static final WireMockRule PUBLIC_API_RULE = new WireMockRule(PUBLIC_API_PORT);

    private static RestClientConfiguration restClientConfiguration;
    private static Client client;
    private static PublicApiRestClient publicApiRestClient;

    static {
        restClientConfiguration = mock(RestClientConfiguration.class);
        when(restClientConfiguration.getConnectTimeout()).thenReturn(FIFTY_SECONDS);
        when(restClientConfiguration.getReadTimeout()).thenReturn(FIFTY_SECONDS);
        client =  RestClientFactory.buildClient(restClientConfiguration);
        publicApiRestClient = new PublicApiRestClient(client, "http://localhost:" + PUBLIC_API_PORT);
    }

    @Test
    public void createPayment_shouldCreateANewPayment() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";
        long amount = 2000;
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";
        String nextUrl = "http://next.url";
        String apiToken = "api-token";
        SupportedLanguage language = SupportedLanguage.WELSH;
        Map<String, String> metadata = Map.of("key", "value");

        JsonObject expectedPaymentRequestPayload = createPaymentRequestPayload(
                amount, reference, description, returnUrl, language.toString(), metadata);
        JsonObject paymentResponsePayload = PublicApiStub.createPaymentResponsePayload(
                paymentId, amount, reference, description, returnUrl, nextUrl, language.toString(),
                metadata);

        setupResponseToCreatePaymentRequest(apiToken, expectedPaymentRequestPayload, paymentResponsePayload);

        PaymentRequest paymentRequest = new PaymentRequest(amount, reference, description, returnUrl, language, metadata, CARD_PAYMENT_LINK);
        PaymentResponse actualPaymentResponse = publicApiRestClient.createPayment(apiToken, paymentRequest);
        
        assertThat(actualPaymentResponse, hasAllPaymentProperties(paymentResponsePayload));
    }

    @Test
    public void createPayment_shouldThrowAnExceptionWhenBadRequest() throws PublicApiResponseErrorException {
        long amount = 2000;
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";
        String apiToken = "api-token";
        SupportedLanguage language = SupportedLanguage.WELSH;

        JsonObject expectedPaymentRequestPayload = createPaymentRequestPayload(
                amount, reference, description, returnUrl, language.toString(), null);
        JsonObject errorPayload = PublicApiStub.createErrorPayload();

        setupResponseToCreatePaymentRequest(apiToken, expectedPaymentRequestPayload, errorPayload, SC_BAD_REQUEST);

        PaymentRequest paymentRequest = new PaymentRequest(amount, reference, description, returnUrl, language, Map.of(), CARD_PAYMENT_LINK);

        try {
            publicApiRestClient.createPayment(apiToken, paymentRequest);
            fail("Expected an PublicApiResponseErrorException to be thrown");
        } catch (PublicApiResponseErrorException exception) {
            assertThat(exception.getErrorStatus(), is(400));
            assertThat(exception.getCode(), is(errorPayload.getString("code")));
            assertThat(exception.getDescription(), is(errorPayload.getString("description")));
        }
    }

    @Test
    public void createPayment_shouldThrowAnExceptionWhenUnauthorized() {
        long amount = 2000;
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";
        String apiToken = "invalid-token";
        SupportedLanguage language = SupportedLanguage.WELSH;

        JsonObject expectedPaymentRequestPayload = createPaymentRequestPayload(
                amount, reference, description, returnUrl, language.toString(), null);

        setupResponseToCreatePaymentRequest(apiToken, expectedPaymentRequestPayload, HttpStatus.SC_UNAUTHORIZED);

        PaymentRequest paymentRequest = new PaymentRequest(amount, reference, description, returnUrl, language, Map.of(), CARD_PAYMENT_LINK);
        try {
            publicApiRestClient.createPayment(apiToken, paymentRequest);
            fail("Expected an PublicApiResponseErrorException to be thrown");
        } catch (PublicApiResponseErrorException exception) {
            assertThat(exception.getErrorStatus(), is(401));
        }
    }

    @Test
    public void findPayment_shouldFindAPayment() {
        long amount = 2000;
        String paymentId = "hu20sqlact5260q2nanm0q8u93";
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";
        String nextUrl = "http://next.url";
        String apiToken = "api-token";
        SupportedLanguage language = SupportedLanguage.WELSH;

        JsonObject paymentResponsePayload = PublicApiStub.createPaymentResponsePayload(
                paymentId, amount, reference, description, returnUrl, nextUrl, language.toString(), null);

        setupResponseToGetPaymentRequest(paymentId, paymentResponsePayload);

        Optional<PaymentResponse> actualPaymentResponse = publicApiRestClient.getPayment(apiToken, paymentId);
        assertTrue(actualPaymentResponse.isPresent());
        assertThat(actualPaymentResponse.get(), hasAllPaymentProperties(paymentResponsePayload));
    }

    @Test
    public void findPayment_shouldNotFindAPayment() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";
        String apiToken = "api-token";

        setupResponseToGetPaymentRequest(paymentId, HttpStatus.SC_NOT_FOUND);

        Optional<PaymentResponse> actualPaymentResponse = publicApiRestClient.getPayment(apiToken, paymentId);
        assertFalse(actualPaymentResponse.isPresent());
    }

    @Test
    public void findPayment_shouldThrowAnException() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";
        String apiToken = "api-token";

        JsonObject errorPayload = PublicApiStub.createErrorPayload();

        setupResponseToGetPaymentRequest(paymentId, errorPayload, SC_BAD_REQUEST);

        try {
            publicApiRestClient.getPayment(apiToken, paymentId);
            fail("Expected an PublicApiResponseErrorException to be thrown");
        } catch (PublicApiResponseErrorException exception) {
            assertThat(exception.getErrorStatus(), is(400));
            assertThat(exception.getCode(), is(errorPayload.getString("code")));
            assertThat(exception.getDescription(), is(errorPayload.getString("description")));
        }
    }
}
