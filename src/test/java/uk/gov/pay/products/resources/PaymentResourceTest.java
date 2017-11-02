package uk.gov.pay.products.resources;

import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.stubs.publicapi.PublicApiStub;

import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentResourceTest extends IntegrationTest {

    private PublicApiStub publicApiStub;

    private String paymentsUrl = "https://products.url/v1/api/payments/";
    private String nextUrl = "www.gov.uk/pay";
    private int gatewayAccountId = randomInt();

    @Before
    public void setup() {
        publicApiStub = new PublicApiStub(mockServerRule.getPort());
    }

    @Test
    public void createAPayment_shouldSucceed() throws Exception {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";

        JsonObject expectedPaymentRequestPayload = PublicApiStub.createPaymentRequestPayload(
                product.getPrice(),
                product.getExternalId(),
                product.getDescription(),
                product.getReturnUrl());
        JsonObject paymentResponsePayload = PublicApiStub.createPaymentResponsePayload(
                govukPaymentId,
                product.getPrice(),
                product.getExternalId(),
                product.getDescription(),
                product.getReturnUrl(),
                nextUrl);
        publicApiStub
                .whenReceiveCreatedPaymentRequestWithBody(expectedPaymentRequestPayload)
                .respondCreatedWithBody(paymentResponsePayload);

        ValidatableResponse response = givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(201);

        List<Map<String, Object>> paymentRecords = databaseHelper.getPaymentsByProductExternalId(product.getExternalId());

        assertThat(paymentRecords.size(), is(1));

        assertThat(paymentRecords.get(0), hasKey("id"));
        assertThat(paymentRecords.get(0), hasKey("external_id"));
        String paymentExternalId = (String) paymentRecords.get(0).get("external_id");

        assertThat(paymentRecords.get(0), hasEntry("govuk_payment_id", govukPaymentId));
        assertThat(paymentRecords.get(0), hasKey("product_id"));
        assertThat(paymentRecords.get(0), hasEntry("next_url", nextUrl));
        assertThat(paymentRecords.get(0), hasEntry("status", "SUCCESS"));
        assertThat(paymentRecords.get(0), hasEntry("amount", product.getPrice()));
        assertThat(paymentRecords.get(0), hasKey("date_created"));

        response
                .body("external_id", is(paymentExternalId))
                .body("govuk_payment_id", is(govukPaymentId))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is("SUCCESS"))
                .body("amount", is(product.getPrice().intValue()))
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + paymentExternalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", is(nextUrl))
                .body("_links[1].method", is(HttpMethod.GET))
                .body("_links[1].rel", is("next"));
    }

    @Test
    public void createAPayment_shouldFail_whenNotAuthenticated() throws Exception {
        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", "payment-id"))
                .then()
                .statusCode(401);
    }

    @Test
    public void createAPayment_shouldFail_whenProductIsNotFound() throws Exception {
        String unknownProductId = "unknown-product-id";

        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", unknownProductId))
                .then()
                .statusCode(404)
                .body("errors", hasSize(1))
                .body("errors[0]", is(format("Product with product id %s not found.", unknownProductId)));
    }

    @Test
    public void createAPayment_shouldFail_whenDownstreamError() throws Exception {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        JsonObject expectedPaymentRequestPayload = PublicApiStub.createPaymentRequestPayload(
                product.getPrice(),
                product.getExternalId(),
                product.getDescription(),
                product.getReturnUrl());
        JsonObject errorPayload = PublicApiStub.createErrorPayload();

        publicApiStub
                .whenReceiveCreatedPaymentRequestWithBody(expectedPaymentRequestPayload)
                .respondBadRequestWithBody(errorPayload);

        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(500)
                .body("errors", hasSize(1))
                .body("errors[0]", is("Downstream system error."));

        List<Map<String, Object>> paymentRecords = databaseHelper.getPaymentsByProductExternalId(product.getExternalId());

        assertThat(paymentRecords.size(), is(1));

        assertThat(paymentRecords.get(0), hasKey("id"));
        assertThat(paymentRecords.get(0), hasKey("external_id"));
        assertThat(paymentRecords.get(0), hasEntry("govuk_payment_id", null));
        assertThat(paymentRecords.get(0), hasKey("product_id"));
        assertThat(paymentRecords.get(0), hasEntry("next_url", null));
        assertThat(paymentRecords.get(0), hasEntry("status", "ERROR"));
        assertThat(paymentRecords.get(0), hasEntry("amount", null));
        assertThat(paymentRecords.get(0), hasKey("date_created"));
    }

    @Test
    public void findAPayment_shouldSucceed() throws Exception {

        String productExternalId = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .build();

        Product product = productEntity.toProduct();

        databaseHelper.addProduct(product);

        Integer productId = databaseHelper.findProductId(productExternalId);
        productEntity.setId(productId);

        String externalId = randomUuid();

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(externalId)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment.toPayment());

        ValidatableResponse response = givenAuthenticatedSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", externalId))
                .then()
                .statusCode(200);

        response
                .body("external_id", is(payment.getExternalId()))
                .body("govuk_payment_id", is(payment.getGovukPaymentId()))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is(payment.getStatus().toString()))
                .body("amount", is(payment.getAmount().intValue()))
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", is(nextUrl))
                .body("_links[1].method", is(HttpMethod.GET))
                .body("_links[1].rel", is("next"));

    }

    @Test
    public void findAPayment_shouldFail_WhenPaymentIsNotFound() throws Exception {
        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void findAPayment_shouldFail_whenNotAuthenticated() throws Exception {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(401);
    }

    @Test
    public void findAllPaymentsOfAProduct_shouldSucceed() throws Exception {

        String productExternalId = randomUuid();
        String paymentExternalId1 = randomUuid();
        String paymentExternalId2 = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .build();

        Product product = productEntity.toProduct();

        databaseHelper.addProduct(product);

        Integer productId = databaseHelper.findProductId(productExternalId);
        productEntity.setId(productId);

        PaymentEntity payment1 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(paymentExternalId1)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment1.toPayment());

        PaymentEntity payment2 = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(paymentExternalId2)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .build();

        databaseHelper.addPayment(payment2.toPayment());

        ValidatableResponse response = givenAuthenticatedSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("v1/api/products/%s/payments", productExternalId))
                .then()
                .statusCode(200);

        response
                .body("", hasSize(2))
                .body("[0].external_id", is(payment1.getExternalId()))
                .body("[0].govuk_payment_id", is(payment1.getGovukPaymentId()))
                .body("[0].product_external_id", is(product.getExternalId()))
                .body("[0].status", is(payment1.getStatus().toString()))
                .body("[0].amount", is(payment1.getAmount().intValue()))
                .body("[0]._links", hasSize(2))
                .body("[0]._links[0].href", matchesPattern(paymentsUrl + payment1.getExternalId()))
                .body("[0]._links[0].method", is(HttpMethod.GET))
                .body("[0]._links[0].rel", is("self"))
                .body("[0]._links[1].href", is(nextUrl))
                .body("[0]._links[1].method", is(HttpMethod.GET))
                .body("[0]._links[1].rel", is("next"))
                .body("[1].external_id", is(payment2.getExternalId()))
                .body("[1].govuk_payment_id", is(payment2.getGovukPaymentId()))
                .body("[1].product_external_id", is(product.getExternalId()))
                .body("[1].status", is(payment2.getStatus().toString()))
                .body("[1].amount", is(payment2.getAmount().intValue()))
                .body("[1]._links", hasSize(2))
                .body("[1]._links[0].href", matchesPattern(paymentsUrl + payment2.getExternalId()))
                .body("[1]._links[0].method", is(HttpMethod.GET))
                .body("[1]._links[0].rel", is("self"))
                .body("[1]._links[1].href", is(nextUrl))
                .body("[1]._links[1].method", is(HttpMethod.GET))
                .body("[1]._links[1].rel", is("next"));
    }

    @Test
    public void findAllPaymentsOfAProduct_shouldFail_whenProductIsNotFound() throws Exception {
        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s/payments", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void findAllPaymentsOfAProduct_shouldFail_whenNotAuthenticated() throws Exception {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(401);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s/payments", randomUuid()))
                .then()
                .statusCode(401);
    }
}