package uk.gov.pay.products.stubs.publicapi;

import org.mockserver.client.server.MockServerClient;

import javax.json.Json;
import javax.json.JsonObject;

import static java.lang.String.format;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;

public class PublicApiStub {
    public static final String API_VERSION_PATH = "/v1";
    private static final String PAYMENTS_PATH = API_VERSION_PATH + "/payments";
    private static final String PAYMENT_PATH = PAYMENTS_PATH + "/%s";

    private final MockServerClient mockClient;

    public PublicApiStub(int mockServerPort) {
        this.mockClient = new MockServerClient("localhost", mockServerPort);
    }

    public static JsonObject createPaymentRequestPayload(long amount, String reference, String description, String returnUrl) {
        return Json.createObjectBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    public static JsonObject createPaymentResponsePayload(String paymentId, long amount, String reference, String description, String returnUrl, String nextUrl) {
        return Json.createObjectBuilder()
                .add("amount", amount)
                .add("state",
                        Json.createObjectBuilder()
                                .add("status", "created")
                                .add("finished", true)
                                .add("message", "User cancelled the payment")
                                .add("code", "P010"))
                .add("description", description)
                .add("reference", reference)
                .add("email", "your email")
                .add("payment_id", paymentId)
                .add("payment_provider", "worldpay")
                .add("return_url", returnUrl)
                .add("created_date", "2016-01-21T17:15:00Z")
                .add("refund_summary",
                        Json.createObjectBuilder()
                                .add("status", "available")
                                .add("amount_available", 1000l)
                                .add("amount_submitted", 2000l))
                .add("settlement_summary",
                        Json.createObjectBuilder()
                                .add("capture_submit_time", "2016-01-21T17:15:00Z")
                                .add("captured_date", "2016-01-21"))
                .add("card_details",
                        Json.createObjectBuilder()
                                .add("last_digits_card_number", "1234")
                                .add("cardholder_name", "Mr. Card holder")
                                .add("expiry_date", "12/20")
                                .add("billing_address",
                                        Json.createObjectBuilder()
                                                .add("line1", "address line 1")
                                                .add("line2", "address line 2")
                                                .add("postcode", "AB1 2CD")
                                                .add("city", "address city")
                                                .add("country", "UK)"))
                                .add("card_brand", "Visa"))
                .add("_links",
                        Json.createObjectBuilder()
                                .add("self",
                                        Json.createObjectBuilder()
                                                .add("href", "https://an.example.link/from/payment/platform")
                                                .add("method", "GET"))
                                .add("next_url",
                                        Json.createObjectBuilder()
                                                .add("href", nextUrl)
                                                .add("method", "GET"))
                                .add("next_url_post",
                                        Json.createObjectBuilder()
                                                .add("type", "multipart/form-data")
                                                .add("params", Json.createObjectBuilder())
                                                .add("href", nextUrl)
                                                .add("method", "POST"))
                                .add("events",
                                        Json.createObjectBuilder()
                                                .add("href", "https://an.example.link/from/payment/platform")
                                                .add("method", "GET"))
                                .add("refunds",
                                        Json.createObjectBuilder()
                                                .add("href", "https://an.example.link/from/payment/platform")
                                                .add("method", "GET"))
                                .add("cancel",
                                        Json.createObjectBuilder()
                                                .add("href", "https://an.example.link/from/payment/platform")
                                                .add("method", "GET")))
                .add("card_brand", "Visa")
                .build();
    }

    public static JsonObject createErrorPayload() {
        return Json.createObjectBuilder()
                .add("field", "a-field")
                .add("code", "a-code")
                .add("description", "A description")
                .build();
    }

    public PublicApiStubExpectation whenReceiveCreatedPaymentRequestWithAuthApiTokenAndWithBody(String authApiToken, JsonObject requestBody) {
        return new PublicApiStubExpectation(mockClient.when(request()
                .withMethod(POST)
                .withPath(PAYMENTS_PATH)
                .withHeader(AUTHORIZATION, "Bearer " + authApiToken)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(requestBody.toString())));
    }

    public PublicApiStubExpectation whenReceiveGetPaymentRequest(String paymentId) {
        return new PublicApiStubExpectation(mockClient.when(request()
                .withMethod(GET)
                .withPath(format(PAYMENT_PATH, paymentId))));
    }
}
