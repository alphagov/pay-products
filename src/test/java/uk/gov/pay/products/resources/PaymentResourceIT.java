package uk.gov.pay.products.resources;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.RandomIdGenerator;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import javax.ws.rs.HttpMethod;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.service.PaymentUpdater.REDACTED_REFERENCE_NUMBER;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.createErrorPayload;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.createPaymentResponsePayload;
import static uk.gov.pay.products.stubs.publicapi.PublicApiStub.setupResponseToCreatePaymentRequest;
import static uk.gov.pay.products.util.PaymentStatus.CREATED;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.ACCOUNT_NOT_LINKED_WITH_PSP;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR;
import static uk.gov.pay.products.util.PublicAPIErrorCodes.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;
import static uk.gov.pay.products.utils.TestHelpers.createPaymentEntity;
import static uk.gov.pay.products.utils.TestHelpers.createProductEntity;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AMOUNT_BELOW_MINIMUM;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED;

public class PaymentResourceIT extends IntegrationTest {

    private final String paymentsUrl = "https://products.url/v1/api/payments/";
    private final String nextUrl = "www.gov.uk/pay";
    private final int gatewayAccountId = randomInt();
    
    private final Appender<ILoggingEvent> mockAppender = mock(Appender.class);;
    private final ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

    @Before
    public void setup() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.addAppender(mockAppender);
        logger.setLevel(Level.WARN);
    }
    
    @Test
    public void deleteHistoricalData() {
        ProductEntity productEntity = addProductToDB(createProductEntity());

        ZonedDateTime now = ZonedDateTime.now();

        PaymentEntity payment1 = addPaymentToDB(createPaymentEntity(productEntity, now, 2));
        PaymentEntity payment2 = addPaymentToDB(createPaymentEntity(productEntity, now, 3));
        PaymentEntity payment3 = addPaymentToDB(createPaymentEntity(productEntity, now, 4));
        PaymentEntity payment4 = addPaymentToDB(createPaymentEntity(productEntity, now, 5));

        givenSetup().accept(APPLICATION_JSON).post("/v1/tasks/delete-historical-data").then().statusCode(OK.getStatusCode());

        givenSetup().get(format("/v1/api/payments/%s", payment1.getExternalId())).then().statusCode(OK.getStatusCode());

        List.of(payment2.getExternalId(), payment3.getExternalId(), payment4.getExternalId()).forEach(paymentExternalId ->
                givenSetup().get(format("/v1/api/payments/%s", paymentExternalId)).then().statusCode(NOT_FOUND.getStatusCode()));
    }

    @Test
    public void redactPaymentReference() {
        ProductEntity productEntity = addProductToDB(createProductEntity());
        PaymentEntity payment = addPaymentToDB(createPaymentEntity(productEntity, CREATED, "4242424242424242", "kts8ici6rm"));
        PaymentEntity paymentThatShouldNotRedacted = addPaymentToDB(createPaymentEntity(productEntity, CREATED, "ABCD1234", "blah"));

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/payments/redact-reference/%s", payment.getGovukPaymentId()))
                .then()
                .statusCode(200);

        givenSetup()
                .get(format("/v1/api/payments/%s", payment.getExternalId()))
                .then()
                .body("reference_number", is(REDACTED_REFERENCE_NUMBER))
                .body("external_id", is(payment.getExternalId()))
                .body("govuk_payment_id", is("kts8ici6rm"));

        givenSetup()
                .get(format("/v1/api/payments/%s", paymentThatShouldNotRedacted.getExternalId()))
                .then()
                .body("reference_number", is("ABCD1234"));
    }

    @Test
    public void createAPayment_shouldSucceed() {
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
                RandomIdGenerator.randomUserFriendlyReference(),
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
        ProductEntity product = addProductToDB(createProductEntity());

        String referenceNumber = randomUuid().substring(1, 10);
        String userDefinedReference = randomUuid().substring(1, 15);
        Long priceOverride = 500L;
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

        Map<String, String> payload = Map.of("price", priceOverride.toString(), "reference_number", userDefinedReference);
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
        ProductEntity productEntity = addProductToDB(createProductEntity());
        
        String userDefinedReference = randomUuid().substring(1, 15);
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

        Map<String, String> payload = Map.of("price", priceOverride.toString(), "reference_number", userDefinedReference);
        givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/payments", productEntity.getExternalId()))
                .then()
                .statusCode(201);
    }

    @Test
    public void createAPayment_shouldSucceed_whenPriceOverrideIsPresent() throws Exception {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);
        
        Long priceOverride = 500L;
        String govukPaymentId = "govukPaymentId";
        String nextUrl = "http://next.url";

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createPaymentResponsePayload(
                govukPaymentId,
                priceOverride,
                RandomIdGenerator.randomUserFriendlyReference(),
                product.getName(),
                product.getReturnUrl(),
                nextUrl,
                product.getLanguage().toString(),
                null));

        Map<String, Long> payload = Map.of("price", priceOverride);
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
    public void createAPayment_shouldReturn403_whenPublicApiReturnsAccountNotLinked() {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), createErrorPayload(null, "P0940", "Account is not fully configured. Please refer to documentation to setup your account or contact support."), HttpStatus.SC_FORBIDDEN);

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(403)
                .body("errors", hasSize(1))
                .body("errors[0]", is("Upstream system error."));
        
        verify(mockAppender, atLeastOnce()).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();
        assertThat(loggingEvents.stream().filter(logEvent -> logEvent.getLevel() == Level.WARN).map(LoggingEvent::getFormattedMessage).collect(Collectors.toList()),
                hasItems("PaymentCreationException thrown due to " + ACCOUNT_NOT_LINKED_WITH_PSP + ". The account is not fully configured."));

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
    public void createAPayment_shouldFail_whenUpstreamError() {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), 
                createErrorPayload(null, "a-code", "A description"), SC_BAD_REQUEST);

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(500)
                .body("errors", hasSize(1))
                .body("errors[0]", is("Upstream system error."));

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
    public void createAPayment_shouldFailAndRedactReference_ForP0105ErrorFromPublicAPI() {
        Product product = aProductEntity()
                .withExternalId(randomUuid())
                .withGatewayAccountId(0)
                .withReferenceEnabled(true)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        setupResponseToCreatePaymentRequest(product.getPayApiToken(), 
                createErrorPayload(null, CREATE_PAYMENT_CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_ERROR, "A description"), 
                SC_BAD_REQUEST);

        Map<String, Object> payload = Map.of("reference_number", "4242424242424242", "price", 100);
        givenSetup()
                .accept(APPLICATION_JSON)
                .body(payload)
                .post(format("/v1/api/products/%s/payments", product.getExternalId()))
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("error_identifier", is(CARD_NUMBER_IN_PAYMENT_LINK_REFERENCE_REJECTED.toString()))
                .body("errors[0]", is("Upstream system error."));

        List<Map<String, Object>> paymentRecords = databaseHelper.getPaymentsByProductExternalId(product.getExternalId());

        assertThat(paymentRecords.size(), is(1));

        assertThat(paymentRecords.get(0), hasKey("id"));
        assertThat(paymentRecords.get(0), hasKey("external_id"));
        assertThat(paymentRecords.get(0), hasEntry("govuk_payment_id", null));
        assertThat(paymentRecords.get(0), hasEntry("reference_number", "****************"));
        assertThat(paymentRecords.get(0), hasKey("product_id"));
        assertThat(paymentRecords.get(0), hasEntry("next_url", null));
        assertThat(paymentRecords.get(0), hasEntry("status", "ERROR"));
        assertThat(paymentRecords.get(0), hasEntry("amount", null));
        assertThat(paymentRecords.get(0), hasKey("date_created"));
    }

    @Test
    public void findAPayment_shouldSucceed() {
        ProductEntity productEntity = addProductToDB(createProductEntity());
        PaymentEntity payment = addPaymentToDB(createPaymentEntity(productEntity, "referenceNumber", productEntity.getGatewayAccountId(), nextUrl));
        
        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/payments/%s", payment.getExternalId()))
                .then()
                .statusCode(200);

        response
                .body("external_id", is(payment.getExternalId()))
                .body("govuk_payment_id", is(payment.getGovukPaymentId()))
                .body("product_external_id", is(productEntity.getExternalId()))
                .body("status", is(payment.getStatus().toString()))
                .body("amount", is(payment.getAmount().intValue()))
                .body("reference_number", is(payment.getReferenceNumber()))
                .body("_links", hasSize(2))
                .body("_links[0].href", matchesPattern(paymentsUrl + payment.getExternalId()))
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
        ProductEntity productEntity = addProductToDB(createProductEntity());
        PaymentEntity payment1 = addPaymentToDB(createPaymentEntity(productEntity, "referenceNumber1", productEntity.getGatewayAccountId(), nextUrl));
        PaymentEntity payment2 = addPaymentToDB(createPaymentEntity(productEntity, "referenceNumber2", productEntity.getGatewayAccountId(), nextUrl));

        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("v1/api/products/%s/payments", productEntity.getExternalId()))
                .then()
                .statusCode(200);

        response
                .body("", hasSize(2))
                .body("[0].external_id", is(payment1.getExternalId()))
                .body("[0].govuk_payment_id", is(payment1.getGovukPaymentId()))
                .body("[0].product_external_id", is(productEntity.getExternalId()))
                .body("[0].status", is(payment1.getStatus().toString()))
                .body("[0].amount", is(payment1.getAmount().intValue()))
                .body("[0].reference_number", is("referenceNumber1"))
                .body("[0]._links", hasSize(2))
                .body("[0]._links[0].href", matchesPattern(paymentsUrl + payment1.getExternalId()))
                .body("[0]._links[0].method", is(HttpMethod.GET))
                .body("[0]._links[0].rel", is("self"))
                .body("[0]._links[1].href", is(nextUrl))
                .body("[0]._links[1].method", is(HttpMethod.GET))
                .body("[0]._links[1].rel", is("next"))
                .body("[1].external_id", is(payment2.getExternalId()))
                .body("[1].govuk_payment_id", is(payment2.getGovukPaymentId()))
                .body("[1].product_external_id", is(productEntity.getExternalId()))
                .body("[1].status", is(payment2.getStatus().toString()))
                .body("[1].amount", is(payment2.getAmount().intValue()))
                .body("[1].reference_number", is("referenceNumber2"))
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
        ProductEntity productEntity = addProductToDB(createProductEntity(gatewayAccountId));

        givenSetup()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/payments", productEntity.getExternalId()))
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("errors[0]", is("User defined reference is enabled but missing"));
    }
}
