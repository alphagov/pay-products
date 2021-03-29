package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.service.payments.commons.model.SupportedLanguage;

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
import static uk.gov.pay.products.fixtures.PaymentEntityFixture.aPaymentEntity;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.createErrorPayload;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.createPaymentResponsePayload;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.setupResponseToCreatePaymentRequest;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class PaymentResourceIT extends IntegrationTest {

    private final String paymentsUrl = "https://products.url/v1/api/payments/";
    private final String nextUrl = "www.gov.uk/pay";
    private final int gatewayAccountId = randomInt();

    @Test
    public void createAPayment_shouldSucceed() {
        String referenceNumber = randomUuid().substring(1, 10);
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createPaymentResponsePayload(
                govukPaymentId,
                product.getPrice(),
                referenceNumber,
                product.getName(),
                product.getReturnUrl(),
                nextUrl,
                product.getLanguage().toString(),
                null));

        ValidatableResponse response = givenSetup()
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
        assertThat(paymentRecords.get(0), hasEntry("status", "SUBMITTED"));
        assertThat(paymentRecords.get(0), hasEntry("amount", product.getPrice()));
        assertThat(paymentRecords.get(0), hasKey("date_created"));
        assertThat(paymentRecords.get(0), hasKey("reference_number"));

        response
                .body("external_id", is(paymentExternalId))
                .body("govuk_payment_id", is(govukPaymentId))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is("SUBMITTED"))
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
    public void createAPaymentWithUserDefinedReference_shouldSucceed() throws Exception {
        String referenceNumber = randomUuid().substring(1, 10);
        String userDefinedReference = randomUuid().substring(1, 15);
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(7)
                .withReferenceEnabled(true)
                .withReferenceLabel("A ref label")
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        Long priceOverride = 500L;
        databaseHelper.addProduct(product);

        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createPaymentResponsePayload(
                govukPaymentId,
                priceOverride,
                referenceNumber,
                product.getName(),
                product.getReturnUrl(),
                nextUrl,
                product.getLanguage().toString(),
                null));

        Map<String, String> payload = ImmutableMap.of("price", priceOverride.toString(), "reference_number", userDefinedReference);
        ValidatableResponse response = givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(201);

        List<Map<String, Object>> paymentRecords = databaseHelper.getPaymentsByProductExternalId(product.getExternalId());

        assertThat(paymentRecords.size(), is(1));

        assertThat(paymentRecords.get(0), hasEntry("amount", priceOverride));

        response
                .body("govuk_payment_id", is(govukPaymentId))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is("SUBMITTED"))
                .body("amount", is(priceOverride.intValue()))
                .body("reference_number", is(userDefinedReference));
    }

    @Test
    public void shouldSucceed_whenUserEnteredReferenceIsEnabled_andReferenceAlreadyExists() throws Exception {
        String userDefinedReference = randomUuid().substring(1, 15);
        String productExternalId = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .withReferenceEnabled(true)
                .withReferenceLabel("Reference label")
                .withLanguage(SupportedLanguage.WELSH)
                .build();

        Product product = productEntity.toProduct();

        databaseHelper.addProduct(product);

        Integer productId = databaseHelper.findProductId(productExternalId);
        productEntity.setId(productId);

        String externalId = randomUuid();

        PaymentEntity payment = aPaymentEntity()
                .withExternalId(externalId)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withGatewayAccountId(productEntity.getGatewayAccountId())
                .withReferenceNumber(userDefinedReference)
                .build();

        databaseHelper.addPayment(payment.toPayment(), productEntity.getGatewayAccountId());

        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";
        Long priceOverride = 501L;

        setupResponseToCreatePaymentRequest(productEntity.getPayApiToken(), createPaymentResponsePayload(
                govukPaymentId,
                priceOverride,
                userDefinedReference,
                productEntity.getName(),
                productEntity.getReturnUrl(),
                nextUrl,
                productEntity.getLanguage().toString(),
                null));

        Map<String, String> payload = ImmutableMap.of("price", priceOverride.toString(), "reference_number", userDefinedReference);
        givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/payments", productEntity.getExternalId()))
                .then()
                .statusCode(201);
    }

    @Test
    public void createAPayment_shouldSucceed_whenPriceOverrideIsPresent() throws Exception {
        String referenceNumber = randomUuid().substring(1, 10);
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        Long priceOverride = 500L;
        databaseHelper.addProduct(product);

        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createPaymentResponsePayload(
                govukPaymentId,
                priceOverride,
                referenceNumber,
                product.getName(),
                product.getReturnUrl(),
                nextUrl,
                product.getLanguage().toString(),
                null));

        Map<String, Long> payload = ImmutableMap.of("price", priceOverride);
        ValidatableResponse response = givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(201);

        List<Map<String, Object>> paymentRecords = databaseHelper.getPaymentsByProductExternalId(product.getExternalId());

        assertThat(paymentRecords.size(), is(1));

        assertThat(paymentRecords.get(0), hasEntry("amount", priceOverride));

        response
                .body("govuk_payment_id", is(govukPaymentId))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is("SUBMITTED"))
                .body("amount", is(priceOverride.intValue()));
    }

    @Test
    public void createAPayment_shouldFail_whenProductIsNotFound() {
        String unknownProductId = "unknown-product-id";

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", unknownProductId))
                .then()
                .statusCode(404)
                .body("errors", hasSize(1))
                .body("errors[0]", is(format("Product with product id %s not found.", unknownProductId)));
    }

    @Test
    public void createAPayment_shouldFail_whenDownstreamError() {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createErrorPayload(), HttpStatus.SC_BAD_REQUEST);

        givenSetup()
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
    public void findAPayment_shouldSucceed() {

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
        String referenceNumber = externalId.substring(0, 9);

        PaymentEntity payment = aPaymentEntity()
                .withExternalId(externalId)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withGatewayAccountId(productEntity.getGatewayAccountId())
                .withReferenceNumber(referenceNumber)
                .build();

        databaseHelper.addPayment(payment.toPayment(), productEntity.getGatewayAccountId());

        ValidatableResponse response = givenSetup()
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
                .body("reference_number", is(referenceNumber))
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", is(nextUrl))
                .body("_links[1].method", is(HttpMethod.GET))
                .body("_links[1].rel", is("next"));

    }

    @Test
    public void findAPayment_shouldFail_WhenPaymentIsNotFound() {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void findAllPaymentsOfAProduct_shouldSucceed() {

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

        PaymentEntity payment1 = aPaymentEntity()
                .withExternalId(paymentExternalId1)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withReferenceNumber(paymentExternalId1.substring(0, 9))
                .build();

        databaseHelper.addPayment(payment1.toPayment(), gatewayAccountId);

        PaymentEntity payment2 = aPaymentEntity()
                .withExternalId(paymentExternalId2)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withReferenceNumber(paymentExternalId2.substring(0, 9))
                .build();

        databaseHelper.addPayment(payment2.toPayment(), gatewayAccountId);

        ValidatableResponse response = givenSetup()
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
                .body("[0].reference_number", is(paymentExternalId1.substring(0, 9)))
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
                .body("[1].reference_number", is(paymentExternalId2.substring(0, 9)))
                .body("[1]._links", hasSize(2))
                .body("[1]._links[0].href", matchesPattern(paymentsUrl + payment2.getExternalId()))
                .body("[1]._links[0].method", is(HttpMethod.GET))
                .body("[1]._links[0].rel", is("self"))
                .body("[1]._links[1].href", is(nextUrl))
                .body("[1]._links[1].method", is(HttpMethod.GET))
                .body("[1]._links[1].rel", is("next"));
    }

    @Test
    public void findAllPaymentsOfAProduct_shouldFail_whenProductIsNotFound() {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s/payments", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void findAPaymentByGatewayAccountIdAndReferenceNumber_shouldSucceed() {

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
        String referenceNumber = externalId.substring(0, 9);

        PaymentEntity payment = aPaymentEntity()
                .withExternalId(externalId)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withGatewayAccountId(productEntity.getGatewayAccountId())
                .withReferenceNumber(referenceNumber)
                .build();

        databaseHelper.addPayment(payment.toPayment(), productEntity.getGatewayAccountId());

        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s/%s", gatewayAccountId, referenceNumber))
                .then()
                .statusCode(200);
        response
                .body("external_id", is(payment.getExternalId()))
                .body("govuk_payment_id", is(payment.getGovukPaymentId()))
                .body("product_external_id", is(product.getExternalId()))
                .body("status", is(payment.getStatus().toString()))
                .body("amount", is(payment.getAmount().intValue()))
                .body("reference_number", is(referenceNumber))
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + externalId))
                .body("_links[0].method", is(HttpMethod.GET))
                .body("_links[0].rel", is("self"))
                .body("_links[1].href", is(nextUrl))
                .body("_links[1].method", is(HttpMethod.GET))
                .body("_links[1].rel", is("next"));
    }

    @Test
    public void shouldReturn404_whenSearchingByGatewayAccountIdAndReferenceNumber_andPaymentIsNonExistent() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s/%s", gatewayAccountId, randomUuid().substring(0, 9)))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldReturn400_whenReferenceEnabledAndNoReferencePresent() {
        String productExternalId = randomUuid();

        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withExternalId(productExternalId)
                .withReferenceEnabled(true)
                .withReferenceLabel("A ref label")
                .build();

        Product product = productEntity.toProduct();
        databaseHelper.addProduct(product);

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("errors[0]", is("User defined reference is enabled but missing"));
    }
}
