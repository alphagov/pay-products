package uk.gov.pay.products.client.publicapi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.products.client.RestClientFactory;
import uk.gov.pay.products.config.RestClientConfiguration;
import uk.gov.pay.products.exception.PublicApiResponseErrorException;
import uk.gov.pay.products.stubs.publicapi.PublicApiStub;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static uk.gov.pay.products.matchers.PaymentResponseMatcher.hasAllPaymentProperties;

public class PublicApiRestClientTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private PublicApiRestClient publicApiRestClient;
    private PublicApiStub publicApiStub;

    @Before
    public void setup() {
        Client client = RestClientFactory.buildClient(mock(RestClientConfiguration.class));
        publicApiRestClient = new PublicApiRestClient(client, "http://localhost:" + mockServerRule.getPort());

        publicApiStub = new PublicApiStub(mockServerRule.getPort());
    }

    @Test
         public void createPayment_shouldCreateANewPayment() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";
        long amount = 2000;
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";
        String nextUrl = "http://next.url";

        JsonObject expectedPaymentRequestPayload = PublicApiStub.createPaymentRequestPayload(amount, reference, description, returnUrl);
        JsonObject paymentResponsePayload = PublicApiStub.createPaymentResponsePayload(paymentId, amount, reference, description, returnUrl, nextUrl);

        publicApiStub
                .whenReceiveCreatedPaymentRequestWithBody(expectedPaymentRequestPayload)
                .respondCreatedWithBody(paymentResponsePayload);

        PaymentRequest paymentRequest = new PaymentRequest(amount, reference, description, returnUrl);
        PaymentResponse actualPaymentResponse = publicApiRestClient.createPayment(paymentRequest);
        assertThat(actualPaymentResponse, hasAllPaymentProperties(paymentResponsePayload));
    }

    @Test
    public void createPayment_shouldThrowAnException() throws PublicApiResponseErrorException {
        long amount = 2000;
        String reference = "a-reference";
        String description = "A Service Description";
        String returnUrl = "http://return.url";

        JsonObject expectedPaymentRequestPayload = PublicApiStub.createPaymentRequestPayload(amount, reference, description, returnUrl);
        JsonObject errorPayload = PublicApiStub.createErrorPayload();

        publicApiStub
                .whenReceiveCreatedPaymentRequestWithBody(expectedPaymentRequestPayload)
                .respondBadRequestWithBody(errorPayload);

        PaymentRequest paymentRequest = new PaymentRequest(amount, reference, description, returnUrl);


        try {
            publicApiRestClient.createPayment(paymentRequest);
            fail("Expected an PublicApiResponseErrorException to be thrown");
        } catch (PublicApiResponseErrorException exception) {
            assertThat(exception.getErrorStatus(), is(400));
            assertThat(exception.getCode(), is(errorPayload.getString("code")));
            assertThat(exception.getDescription(), is(errorPayload.getString("description")));
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

        JsonObject paymentResponsePayload = PublicApiStub.createPaymentResponsePayload(paymentId, amount, reference, description, returnUrl, nextUrl);

        publicApiStub
                .whenReceiveGetPaymentRequest(paymentId)
                .respondOkWithBody(paymentResponsePayload);

        Optional<PaymentResponse> actualPaymentResponse = publicApiRestClient.getPayment(paymentId);
        assertTrue(actualPaymentResponse.isPresent());
        assertThat(actualPaymentResponse.get(), hasAllPaymentProperties(paymentResponsePayload));
    }

    @Test
    public void findPayment_shouldNotFindAPayment() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";

        publicApiStub
                .whenReceiveGetPaymentRequest(paymentId)
                .respondNotFound();

        Optional<PaymentResponse> actualPaymentResponse = publicApiRestClient.getPayment(paymentId);
        assertFalse(actualPaymentResponse.isPresent());
    }

    @Test
    public void findPayment_shouldThrowAnException() {
        String paymentId = "hu20sqlact5260q2nanm0q8u93";

        JsonObject errorPayload = PublicApiStub.createErrorPayload();

        publicApiStub
                .whenReceiveGetPaymentRequest(paymentId)
                .respondBadRequestWithBody(errorPayload);

        try {
            publicApiRestClient.getPayment(paymentId);
            fail("Expected an PublicApiResponseErrorException to be thrown");
        } catch (PublicApiResponseErrorException exception) {
            assertThat(exception.getErrorStatus(), is(400));
            assertThat(exception.getCode(), is(errorPayload.getString("code")));
            assertThat(exception.getDescription(), is(errorPayload.getString("description")));
        }
    }
}