package uk.gov.pay.products.stubs.publicapi;

import org.apache.http.HttpStatus;

import javax.json.Json;
import javax.json.JsonObject;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class PublicApiStub {
    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String PAYMENT_PATH = PAYMENTS_PATH + "/%s";
    
    public static JsonObject createPaymentRequestPayload(long amount, String reference, String description, String returnUrl, String language) {
        return Json.createObjectBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .add("language", language)
                .add("internal", Json.createObjectBuilder().add("source","CARD_PAYMENT_LINK"))
                .build();
    }

    public static JsonObject createPaymentResponsePayload(String paymentId,
                                                          long amount,
                                                          String reference,
                                                          String description,
                                                          String returnUrl,
                                                          String nextUrl,
                                                          String language) {
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
                .add("language", language)
                .add("email", "your email")
                .add("payment_id", paymentId)
                .add("payment_provider", "worldpay")
                .add("return_url", returnUrl)
                .add("created_date", "2016-01-21T17:15:00Z")
                .add("refund_summary",
                        Json.createObjectBuilder()
                                .add("status", "available")
                                .add("amount_available", 1000L)
                                .add("amount_submitted", 2000L))
                .add("settlement_summary",
                        Json.createObjectBuilder()
                                .add("capture_submit_time", "2016-01-21T17:15:00Z")
                                .add("captured_date", "2016-01-21"))
                .add("card_details",
                        Json.createObjectBuilder()
                                .add("last_digits_card_number", "1234")
                                .add("first_digits_card_number", "123456")
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
                .add("Do not add this property to PaymentResponse", "To test deserialisation handles new/unknown properties")
                .build();
    }

    public static JsonObject createErrorPayload() {
        return Json.createObjectBuilder()
                .add("field", "a-field")
                .add("code", "a-code")
                .add("description", "A description")
                .build();
    }

    public static void setupResponseToCreatePaymentRequest(String authApiToken, JsonObject responseBody) {
        setupResponseToCreatePaymentRequest(authApiToken, responseBody, HttpStatus.SC_CREATED);
    }

    public static void setupResponseToCreatePaymentRequest(String authApiToken, JsonObject responseBody, int responseStatusCode) {
        stubFor(post(urlPathEqualTo(PAYMENTS_PATH))
                .withHeader(AUTHORIZATION, matching("Bearer " + authApiToken))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(responseStatusCode)
                        .withBody(responseBody.toString())));
    }

    public static void setupResponseToCreatePaymentRequest(String authApiToken, JsonObject requestBody, JsonObject responseBody) {
        setupResponseToCreatePaymentRequest(authApiToken, requestBody, responseBody, HttpStatus.SC_CREATED);
    }

    public static void setupResponseToCreatePaymentRequest(String authApiToken, JsonObject requestBody, JsonObject responseBody, int responseStatusCode) {
        stubFor(post(urlPathEqualTo(PAYMENTS_PATH))
                .withHeader(AUTHORIZATION, matching("Bearer " + authApiToken))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON))
                .withRequestBody(equalToJson(requestBody.toString()))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(responseStatusCode)
                        .withBody(responseBody.toString())));
    }

    public static void setupResponseToGetPaymentRequest(String paymentId, JsonObject paymentResponsePayload) {
        setupResponseToGetPaymentRequest(paymentId, paymentResponsePayload, HttpStatus.SC_OK);
    }

    public static void setupResponseToGetPaymentRequest(String paymentId, JsonObject paymentResponsePayload, int responseStatus) {
        stubFor(get(urlPathEqualTo(format(PAYMENT_PATH, paymentId)))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(responseStatus)
                        .withBody(paymentResponsePayload.toString())));
    }

    public static void setupResponseToGetPaymentRequest(String paymentId, int responseStatus) {
        stubFor(get(urlPathEqualTo(format(PAYMENT_PATH, paymentId)))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(responseStatus)));
    }
}
