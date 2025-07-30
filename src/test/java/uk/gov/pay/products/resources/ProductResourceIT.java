package uk.gov.pay.products.resources;

import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.products.extension.ProductsAppWithPostgresExtension;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import jakarta.ws.rs.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.products.fixtures.PaymentEntityFixture.aPaymentEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductResourceIT {

    private static final String PAY_API_TOKEN = "pay_api_token";
    private static final String NAME = "name";
    private static final String PRICE = "price";
    private static final String EXTERNAL_ID = "external_id";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String RETURN_URL = "return_url";
    private static final String GATEWAY_ACCOUNT_ID = "gateway_account_id";
    private static final String SERVICE_NAME_PATH = "service_name_path";
    private static final String PRODUCT_NAME_PATH = "product_name_path";
    private static final String REFERENCE_ENABLED_FIELD = "reference_enabled";
    private static final String REFERENCE_LABEL = "reference_label";
    private static final String REFERENCE_HINT = "reference_hint";
    private static final String AMOUNT_HINT = "amount_hint";
    private static final String LANGUAGE = "language";
    private static final String METADATA = "metadata";
    private static final String REQUIRE_CAPTCHA = "require_captcha";
    private static final String DATE_CREATED = "date_created";
    private static final RandomStringUtils randomStringUtils = RandomStringUtils.insecure();

    @RegisterExtension
    private static final ProductsAppWithPostgresExtension app = new ProductsAppWithPostgresExtension();

    @Nested
    class CreateProduct {

        @Test
         void shouldSuccess_whenSavingAValidProduct_withMinimumMandatoryFields() throws Exception {

            String payApiToken = randomUuid();
            String name = "Flashy new GOV Service";
            Long price = 1050L;
            Integer gatewayAccountId = randomInt();
            String type = ProductType.DEMO.name();

            Map<Object, Object> payload = Map.of(
                    GATEWAY_ACCOUNT_ID, gatewayAccountId,
                    PAY_API_TOKEN, payApiToken,
                    NAME, name,
                    PRICE, price,
                    TYPE, type,
                    RETURN_URL, "https://return.url"
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(201);

            response
                    .body(NAME, is("Flashy new GOV Service"))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(PRICE, is(1050))
                    .body(EXTERNAL_ID, matchesPattern("^[0-9a-z]{32}$"))
                    .body(TYPE, is(type))
                    .body(LANGUAGE, is("en"));

            String externalId = response.extract().path(EXTERNAL_ID);

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            response
                    .body("_links", hasSize(2))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"));
        }

        @Test
         void shouldSuccess_whenSavingAValidProduct_withAllFieldsReferenceNotEnabled() throws Exception {

            String payApiToken = randomUuid();
            String name = "Flashy new GOV Service";
            long price = 1050L;
            String description = "Some test description";
            Integer gatewayAccountId = randomInt();
            String type = ProductType.ADHOC.name();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);
            String returnUrl = "https://some.valid.url";
            String language = "cy";

            Map<String, String> payload = Map.ofEntries(
                    Map.entry(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString()),
                    Map.entry(PAY_API_TOKEN, payApiToken),
                    Map.entry(NAME, name),
                    Map.entry(PRICE, Long.toString(price)),
                    Map.entry(DESCRIPTION, description),
                    Map.entry(TYPE, type),
                    Map.entry(RETURN_URL, returnUrl),
                    Map.entry(SERVICE_NAME_PATH, serviceNamePath),
                    Map.entry(PRODUCT_NAME_PATH, productNamePath),
                    Map.entry(REFERENCE_ENABLED_FIELD, Boolean.FALSE.toString()),
                    Map.entry(LANGUAGE, language)
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(201);

            response
                    .body(NAME, is(name))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(PRICE, is(1050))
                    .body(TYPE, is(type))
                    .body(DESCRIPTION, is(description))
                    .body(RETURN_URL, is(returnUrl))
                    .body(REFERENCE_ENABLED_FIELD, is(false))
                    .body(LANGUAGE, is(language));

            String externalId = response.extract().path(EXTERNAL_ID);

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            String urlToMatch = format("https://products-ui.url/products/%s/%s", serviceNamePath, productNamePath);
            response
                    .body("_links", hasSize(3))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"))
                    .body("_links[2].href", is(urlToMatch))
                    .body("_links[2].method", is(HttpMethod.GET))
                    .body("_links[2].rel", is("friendly"));

        }

        @Test
         void shouldSuccess_whenSavingAValidProduct_withAllFieldsReferenceEnabled() throws Exception {

            String payApiToken = randomUuid();
            String name = "Flashy new GOV Service";
            long price = 1050L;
            String description = "Some test description";
            Integer gatewayAccountId = randomInt();
            String type = ProductType.ADHOC.name();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);
            String referenceLabel = randomStringUtils.nextAlphanumeric(25);
            String referenceHint = randomStringUtils.nextAlphanumeric(85);
            String amountHint = "An amount hint";
            String returnUrl = "https://some.valid.url";
            String language = "en";

            Map<String, String> payload = Map.ofEntries(
                    Map.entry(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString()),
                    Map.entry(PAY_API_TOKEN, payApiToken),
                    Map.entry(NAME, name),
                    Map.entry(PRICE, Long.toString(price)),
                    Map.entry(DESCRIPTION, description),
                    Map.entry(TYPE, type),
                    Map.entry(RETURN_URL, returnUrl),
                    Map.entry(SERVICE_NAME_PATH, serviceNamePath),
                    Map.entry(PRODUCT_NAME_PATH, productNamePath),
                    Map.entry(REFERENCE_ENABLED_FIELD, Boolean.TRUE.toString()),
                    Map.entry(REFERENCE_LABEL, referenceLabel),
                    Map.entry(REFERENCE_HINT, referenceHint),
                    Map.entry(AMOUNT_HINT, amountHint),
                    Map.entry(LANGUAGE, language)
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(201);

            response
                    .body(NAME, is(name))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(PRICE, is(1050))
                    .body(TYPE, is(type))
                    .body(DESCRIPTION, is(description))
                    .body(RETURN_URL, is(returnUrl))
                    .body(REFERENCE_ENABLED_FIELD, is(true))
                    .body(REFERENCE_LABEL, is(referenceLabel))
                    .body(REFERENCE_HINT, is(referenceHint))
                    .body(AMOUNT_HINT, is(amountHint))
                    .body(LANGUAGE, is(language))
                    .body(REQUIRE_CAPTCHA, is(false));

            String externalId = response.extract().path(EXTERNAL_ID);

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            String urlToMatch = format("https://products-ui.url/products/%s/%s", serviceNamePath, productNamePath);
            response
                    .body("_links", hasSize(3))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + "reference/" + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"))
                    .body("_links[2].href", is(urlToMatch))
                    .body("_links[2].method", is(HttpMethod.GET))
                    .body("_links[2].rel", is("friendly"));

        }

        @Test
         void shouldError_whenSavingAProduct_withMandatoryFieldsMissing() throws Exception {
            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(Map.of())
                    .post("/v1/api/products")
                    .then()
                    .statusCode(400);
        }

        @Test
         void shouldError_whenSavingAProduct_withProductPathAlreadyExisting() throws Exception {
            String externalId = randomUuid();
            Integer gatewayAccountId = randomInt();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .withType(ProductType.ADHOC)
                    .withPrice(1000)
                    .withProductPath(serviceNamePath, productNamePath)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            String payApiToken = randomUuid();
            String name = "Existing product path product attempt";
            long price = 1050L;
            String description = "Some test description";
            String type = ProductType.ADHOC.name();

            String returnUrl = "https://some.valid.url";

            Map<String, String> payload = Map.ofEntries(
                    Map.entry(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString()),
                    Map.entry(PAY_API_TOKEN, payApiToken),
                    Map.entry(NAME, name),
                    Map.entry(PRICE, Long.toString(price)),
                    Map.entry(DESCRIPTION, description),
                    Map.entry(TYPE, type),
                    Map.entry(RETURN_URL, returnUrl),
                    Map.entry(SERVICE_NAME_PATH, serviceNamePath),
                    Map.entry(PRODUCT_NAME_PATH, productNamePath)
            );

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(409);
        }

        @Test
         void shouldReturn400_whenSavingProduct_withReferenceEnabledAndNoReferenceLabel() throws Exception {

            String payApiToken = randomUuid();
            String name = "Flashy new GOV Service";
            long price = 1050L;
            String description = "Some test description";
            Integer gatewayAccountId = randomInt();
            String type = ProductType.ADHOC.name();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);
            String referenceHint = randomStringUtils.nextAlphanumeric(85);

            String returnUrl = "https://some.valid.url";

            Map<String, String> payload = Map.ofEntries(
                    Map.entry(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString()),
                    Map.entry(PAY_API_TOKEN, payApiToken),
                    Map.entry(NAME, name),
                    Map.entry(PRICE, Long.toString(price)),
                    Map.entry(DESCRIPTION, description),
                    Map.entry(TYPE, type),
                    Map.entry(RETURN_URL, returnUrl),
                    Map.entry(SERVICE_NAME_PATH, serviceNamePath),
                    Map.entry(PRODUCT_NAME_PATH, productNamePath),
                    Map.entry(REFERENCE_ENABLED_FIELD, Boolean.TRUE.toString()),
                    Map.entry(REFERENCE_HINT, referenceHint)
            );

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(400);
        }

        @Test
         void shouldSuccess_whenSavingAValidProduct_withMetadataFields() throws Exception {

            String payApiToken = randomUuid();
            String name = "Flashy new GOV Service with metadata";
            Long price = 1050L;
            Integer gatewayAccountId = randomInt();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);
            String type = ProductType.ADHOC.name();

            Map<Object, Object> payload = Map.ofEntries(
                    Map.entry(GATEWAY_ACCOUNT_ID, gatewayAccountId),
                    Map.entry(PAY_API_TOKEN, payApiToken),
                    Map.entry(NAME, name),
                    Map.entry(PRICE, price),
                    Map.entry(TYPE, type),
                    Map.entry(SERVICE_NAME_PATH, serviceNamePath),
                    Map.entry(PRODUCT_NAME_PATH, productNamePath),
                    Map.entry(METADATA, Map.of("key1", "value1", "key2", "value2")),
                    Map.entry(RETURN_URL, "https://return.url")
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .post("/v1/api/products")
                    .then()
                    .statusCode(201);

            String externalId = response.extract().path(EXTERNAL_ID);

            List<Map<String, Object>> metadata = app.getDatabaseTestHelper().findMetadataByProductExternalId(externalId);

            assertThat(metadata.size(), is(2));
            assertThat(metadata.get(0), hasEntry("metadata_key", "key1"));
            assertThat(metadata.get(0), hasEntry("metadata_value", "value1"));
            assertThat(metadata.get(1), hasEntry("metadata_key", "key2"));
            assertThat(metadata.get(1), hasEntry("metadata_value", "value2"));
        }
    }

    @Nested
    class FindProductByExternalId {
        @Test
         void findProductByExternalId_shouldReturnProduct_whenFound() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .withRequireCaptcha(true)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products/%s", externalId))
                    .then()
                    .statusCode(200);

            Integer intPrice = response.extract().path(PRICE);
            Long price = Long.valueOf(intPrice);
            assertThat(price, equalTo(product.getPrice()));

            response
                    .body(NAME, is(product.getName()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(TYPE, is(product.getType().name()))
                    .body(DESCRIPTION, is(product.getDescription()))
                    .body(RETURN_URL, is(product.getReturnUrl()))
                    .body(REQUIRE_CAPTCHA, is(true));

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            response
                    .body("_links", hasSize(2))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"));
        }

        @Test
         void findProductByExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .accept(APPLICATION_JSON)
                    .get("/v1/api/products/999999999")
                    .then()
                    .statusCode(404);
        }

        @Test
         void shouldReturnMetadata_whenMetadataExistsForAProduct() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.getDatabaseTestHelper().addMetadata(externalId, "key", "value");
            app.getDatabaseTestHelper().addMetadata(externalId, "secondkey", "value2");

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products/%s", externalId))
                    .then()
                    .statusCode(200);

            Integer intPrice = response.extract().path(PRICE);
            Long price = Long.valueOf(intPrice);
            assertThat(price, equalTo(product.getPrice()));

            response
                    .body(NAME, is(product.getName()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(TYPE, is(product.getType().name()))
                    .body(DESCRIPTION, is(product.getDescription()))
                    .body(RETURN_URL, is(product.getReturnUrl()))
                    .body(METADATA + ".key", is("value"))
                    .body(METADATA + ".secondkey", is("value2"));
        }
    }

    @Nested
    class FindProductByGatewayAccountIdAndExternalId {
        @Test
         void findProductByGatewayAccountIdAndExternalId_shouldReturnProduct_whenFound() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            Integer intPrice = response.extract().path(PRICE);
            Long price = Long.valueOf(intPrice);
            assertThat(price, equalTo(product.getPrice()));

            response
                    .body(NAME, is(product.getName()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(TYPE, is(product.getType().name()))
                    .body(DESCRIPTION, is(product.getDescription()))
                    .body(RETURN_URL, is(product.getReturnUrl()))
                    .body(LANGUAGE, is("en"))
                    .body(DATE_CREATED, is(notNullValue()));

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            response
                    .body("_links", hasSize(2))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"));
        }

        @Test
         void findProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products/%s", randomInt(), randomUuid()))
                    .then()
                    .statusCode(404);
        }
    }
    
    @Nested
    class DisableProductByExternalId {
        @Test
         void disableProductByExternalId_shouldReturn201_whenProductIsDisabled() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .patch(format("/v1/api/products/%s/disable", externalId))
                    .then()
                    .statusCode(204);

        }

        @Test
         void disableProductByExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .patch(format("/v1/api/products/%s/disable", randomUuid()))
                    .then()
                    .statusCode(404);
        }
    }
    
    @Nested
    class UpdateProduct {
        @Test
         void updateProduct_shouldUpdateProduct_whenFound_referenceEnabled() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";
            String updatedReferenceLabel = "updated-reference-label";
            String updatedReferenceHint = "updated-reference-hint";
            String updatedAmountHint = "updated-amount-hint";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .withRequireCaptcha(false)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, true),
                    Map.entry(REFERENCE_LABEL, updatedReferenceLabel),
                    Map.entry(REFERENCE_HINT, updatedReferenceHint),
                    Map.entry(AMOUNT_HINT, updatedAmountHint)
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            response
                    .body(NAME, is(updatedName))
                    .body(DESCRIPTION, is(updatedDescription))
                    .body(PRICE, is(Integer.valueOf(updatedPrice)))
                    .body(REFERENCE_ENABLED_FIELD, is(true))
                    .body(REFERENCE_LABEL, is(updatedReferenceLabel))
                    .body(REFERENCE_HINT, is(updatedReferenceHint))
                    .body(AMOUNT_HINT, is(updatedAmountHint))
                    .body(TYPE, is(existingProduct.getType().name()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(RETURN_URL, is(existingProduct.getReturnUrl()))
                    .body(LANGUAGE, is("en"))
                    .body(REQUIRE_CAPTCHA, is(false));

            String productsUrl = "https://products.url/v1/api/products/";
            String productsUIPayUrl = "https://products-ui.url/pay/";
            String productFriendlyUrl = format("https://products-ui.url/products/%s/%s",
                    existingProduct.getServiceNamePath(), existingProduct.getProductNamePath());
            response
                    .body("_links", hasSize(3))
                    .body("_links[0].href", matchesPattern(productsUrl + externalId))
                    .body("_links[0].method", is(HttpMethod.GET))
                    .body("_links[0].rel", is("self"))
                    .body("_links[1].href", matchesPattern(productsUIPayUrl + "reference/" + externalId))
                    .body("_links[1].method", is(HttpMethod.GET))
                    .body("_links[1].rel", is("pay"))
                    .body("_links[2].href", matchesPattern(productFriendlyUrl))
                    .body("_links[2].method", is(HttpMethod.GET))
                    .body("_links[2].rel", is("friendly"));

            List<Map<String, Object>> productsRecords = app.getDatabaseTestHelper().findProductEntityByGatewayAccountId(gatewayAccountId);
            assertThat(productsRecords.size(), is(1));
            Map<String, Object> productRecord = productsRecords.getFirst();
            assertThat(productRecord, hasEntry("name", updatedName));
            assertThat(productRecord, hasEntry("description", updatedDescription));
            assertThat(productRecord, hasEntry("price", Long.valueOf(updatedPrice)));
            assertThat(productRecord, hasEntry("reference_enabled", true));
            assertThat(productRecord, hasEntry("reference_label", updatedReferenceLabel));
            assertThat(productRecord, hasEntry("reference_hint", updatedReferenceHint));
            assertThat(productRecord, hasEntry("amount_hint", updatedAmountHint));
        }

        @Test
         void updateProduct_shouldUpdateProduct_whenFound_referenceEnabledNoHint() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";
            String updatedReferenceLabel = "updated-reference-label";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, true),
                    Map.entry(REFERENCE_LABEL, updatedReferenceLabel)
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            response
                    .body(NAME, is(updatedName))
                    .body(DESCRIPTION, is(updatedDescription))
                    .body(PRICE, is(Integer.valueOf(updatedPrice)))
                    .body(REFERENCE_ENABLED_FIELD, is(true))
                    .body(REFERENCE_LABEL, is(updatedReferenceLabel))
                    .body(TYPE, is(existingProduct.getType().name()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(RETURN_URL, is(existingProduct.getReturnUrl()))
                    .body(LANGUAGE, is("en"));

            List<Map<String, Object>> productsRecords = app.getDatabaseTestHelper().findProductEntityByGatewayAccountId(gatewayAccountId);
            assertThat(productsRecords.size(), is(1));
            assertThat(productsRecords.getFirst(), hasEntry("name", updatedName));
            assertThat(productsRecords.getFirst(), hasEntry("description", updatedDescription));
            assertThat(productsRecords.getFirst(), hasEntry("price", Long.valueOf(updatedPrice)));
            assertThat(productsRecords.getFirst(), hasEntry("reference_enabled", true));
            assertThat(productsRecords.getFirst(), hasEntry("reference_label", updatedReferenceLabel));
            assertThat(productsRecords.getFirst(), hasEntry("reference_hint", null));
        }

        @Test
         void updateProduct_shouldUpdateProduct_whenFound_referenceDisabled() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, false)
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            response
                    .body(NAME, is(updatedName))
                    .body(DESCRIPTION, is(updatedDescription))
                    .body(PRICE, is(Integer.valueOf(updatedPrice)))
                    .body(REFERENCE_ENABLED_FIELD, is(false))
                    .body(TYPE, is(existingProduct.getType().name()))
                    .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                    .body(RETURN_URL, is(existingProduct.getReturnUrl()))
                    .body(LANGUAGE, is("en"));

            List<Map<String, Object>> productsRecords = app.getDatabaseTestHelper().findProductEntityByGatewayAccountId(gatewayAccountId);
            assertThat(productsRecords.size(), is(1));
            assertThat(productsRecords.getFirst(), hasEntry("name", updatedName));
            assertThat(productsRecords.getFirst(), hasEntry("description", updatedDescription));
            assertThat(productsRecords.getFirst(), hasEntry("price", Long.valueOf(updatedPrice)));
            assertThat(productsRecords.getFirst(), hasEntry("reference_enabled", false));
            assertThat(productsRecords.getFirst(), hasEntry("reference_label", null));
            assertThat(productsRecords.getFirst(), hasEntry("reference_hint", null));
        }

        @Test
         void updateProduct_shouldAddMetadataCorrectly() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, false),
                    Map.entry(METADATA, Map.of("key1", "value1", "key3", "value3"))
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            response
                    .body(METADATA + ".key1", is("value1"))
                    .body(METADATA + ".key3", is("value3"));

            List<Map<String, Object>> metadataByProductExternalId = app.getDatabaseTestHelper().findMetadataByProductExternalId(externalId);
            assertThat(metadataByProductExternalId.size(), is(2));
            assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_key", "key1"));
            assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_value", "value1"));
            assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_key", "key3"));
            assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_value", "value3"));
        }

        @Test
         void updateProduct_shouldReplaceExistingMetadataCorrectly() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);
            app.getDatabaseTestHelper().addMetadata(externalId, "key1", "value1");
            app.getDatabaseTestHelper().addMetadata(externalId, "key2", "value2");

            List<Map<String, Object>> metadataByProductExternalId = app.getDatabaseTestHelper().findMetadataByProductExternalId(externalId);
            assertThat(metadataByProductExternalId.size(), is(2));

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, false),
                    Map.entry(METADATA, Map.of("key1", "new_value1", "key3", "value3"))
            );

            ValidatableResponse response = app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200);

            response
                    .body(METADATA + ".key1", is("new_value1"))
                    .body(METADATA + ".key3", is("value3"));

            metadataByProductExternalId = app.getDatabaseTestHelper().findMetadataByProductExternalId(externalId);
            assertThat(metadataByProductExternalId.size(), is(2));
            assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_key", "key1"));
            assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_value", "new_value1"));
            assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_key", "key3"));
            assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_value", "value3"));
        }

        @Test
         void updateProduct_shouldReturn404_whenNotFound() throws Exception {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();
            int anotherGatewayAccountId = randomInt();

            String updatedName = "updated-name";
            String updatedDescription = "updated-description";
            String updatedPrice = "1000";

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withName("name")
                    .withDescription("description")
                    .withPrice(500)
                    .withGatewayAccountId(gatewayAccountId)
                    .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            Map<String, Object> payload = Map.ofEntries(
                    Map.entry(NAME, updatedName),
                    Map.entry(PRICE, updatedPrice),
                    Map.entry(DESCRIPTION, updatedDescription),
                    Map.entry(REFERENCE_ENABLED_FIELD, false),
                    Map.entry(METADATA, Map.of("key1", "new_value1", "key3", "value3"))
            );

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v1/api/gateway-account/%s/products/%s", anotherGatewayAccountId, externalId))
                    .then()
                    .statusCode(404);
        }

        @Test
         void shouldSucceed_updateProductUsingJsonPatch() throws Exception {
            String externalId = randomUuid();
            Integer gatewayAccountId = randomInt();

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .withRequireCaptcha(false)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            var payload = List.of(
                    Map.of("path", "require_captcha",
                            "op", "replace",
                            "value", true));

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v2/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(200)
                    .body("require_captcha", is(true));

            Optional<Map<String, Object>> updatedProduct = app.getDatabaseTestHelper().findProductEntityByExternalId(externalId);
            assertThat(updatedProduct.isPresent(), is(true));
            assertThat(updatedProduct.get(), hasEntry("require_captcha", true));
        }

        @Test
         void shouldReturn400_updateProductUsingJsonPatch_invalidRequest() throws Exception {
            String externalId = randomUuid();
            Integer gatewayAccountId = randomInt();

            Product existingProduct = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .withRequireCaptcha(false)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(existingProduct);

            var payload = singletonList(Map.of("path", "require_captcha",
                    "op", "replace"));

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v2/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(400)
                    .body("errors[0]", is("Field [value] is required"));
        }

        @Test
         void shouldReturn404_updateProductUsingJsonPatch_productNotFound() throws Exception {
            String externalId = randomUuid();
            Integer gatewayAccountId = randomInt();

            var payload = singletonList(Map.of("path", "require_captcha",
                    "op", "replace",
                    "value", true));

            app.givenSetup()
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON)
                    .body(payload)
                    .patch(format("/v2/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class DeleteProductByExternalId {
        @Test
         void deleteProductByExternalId_shouldReturn201_whenProductIsDeleted() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .delete(format("/v1/api/products/%s", externalId))
                    .then()
                    .statusCode(204);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products/%s", externalId))
                    .then()
                    .statusCode(404);

        }

        @Test
         void deleteProductByExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .delete(format("/v1/api/products/%s", randomUuid()))
                    .then()
                    .statusCode(404);
        }
    }
    
    @Nested
    class DeleteProductByGatewayAccountIdAndExternalId {
        @Test
         void deleteProductByGatewayAccountIdAndExternalId_shouldReturn201_whenProductIsDeleted() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .delete(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                    .then()
                    .statusCode(204);
        }

        @Test
         void deleteProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .delete(format("/v1/api/gateway-account/%s/products/%s", randomInt(), randomUuid()))
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class DisableProductByGatewayAccountIdAndExternalId {
        @Test
         void disableProductByGatewayAccountIdAndExternalId_shouldReturn201_whenProductIsDisabled() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .patch(format("/v1/api/gateway-account/%s/products/%s/disable", gatewayAccountId, externalId))
                    .then()
                    .statusCode(204);

        }

        @Test
         void disableProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .patch(format("/v1/api/gateway-account/%s/products/%s/disable", randomInt(), randomUuid()))
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class FindProductsByGatewayAccountId {
        @Test
         void findProductsByGatewayAccountId_shouldReturnActiveProducts_whenFound() {
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            Product product_2 = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product_2);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products", gatewayAccountId))
                    .then()
                    .statusCode(200);

            response.body("", hasSize(2))
                    .body("[0].gateway_account_id", is(gatewayAccountId))
                    .body("[0]._links", hasSize(2))
                    .body("[0].description", matchesPattern(product.getDescription()))
                    .body("[0].price", is(product.getPrice().intValue()))
                    .body("[0].name", matchesPattern(product.getName()))
                    .body("[1].name", matchesPattern(product_2.getName()));
        }

        @Test
         void findProductsByGatewayAccountIdWithType_shouldReturnOnlyProductsWithMatchingType_whenFound() {
            int gatewayAccountId = randomInt();

            Product productWithCorrectType = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .withType(ProductType.AGENT_INITIATED_MOTO)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(productWithCorrectType);

            Product productWithIncorrectType = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .withType(ProductType.ADHOC)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(productWithIncorrectType);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products?type=%s", gatewayAccountId, ProductType.AGENT_INITIATED_MOTO))
                    .then()
                    .statusCode(200);

            response.body("", hasSize(1))
                    .body("[0].gateway_account_id", is(gatewayAccountId))
                    .body("[0]._links", hasSize(2))
                    .body("[0].description", matchesPattern(productWithCorrectType.getDescription()))
                    .body("[0].price", is(productWithCorrectType.getPrice().intValue()))
                    .body("[0].name", matchesPattern(productWithCorrectType.getName()));
        }

        @Test
         void findProductsByGatewayAccountId_shouldReturnNoProduct_whenNoneFound() {
            int unknownGatewayAccountId = randomInt();
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products", unknownGatewayAccountId))
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }

        @Test
         void findProductsByGatewayAccountIdWithType_shouldReturnNoProduct_whenNoneFound() {
            int unknownGatewayAccountId = randomInt();
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products?type=%s", unknownGatewayAccountId, ProductType.DEMO))
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }

        @Test
         void findProductsByGatewayAccountId_shouldNotReturnInactiveProducts() {
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .withStatus(ProductStatus.INACTIVE)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products", gatewayAccountId))
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }

        @Test
         void findProductsByGatewayAccountIdWithType_shouldNotReturnInactiveProducts() {
            int gatewayAccountId = randomInt();

            Product product = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(gatewayAccountId)
                    .withStatus(ProductStatus.INACTIVE)
                    .withType(ProductType.PROTOTYPE)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/gateway-account/%s/products?type=%s", gatewayAccountId, ProductType.PROTOTYPE))
                    .then()
                    .statusCode(200)
                    .body("", hasSize(0));
        }
    }

    @Nested
    class FindProductByProductPath {
        @Test
         void findProductByProductPath_shouldReturnProduct_whenFound() {
            String externalId = randomUuid();
            int gatewayAccountId = randomInt();
            String serviceNamePath = randomStringUtils.nextAlphanumeric(40);
            String productNamePath = randomStringUtils.nextAlphanumeric(65);

            Product product = ProductEntityFixture.aProductEntity()
                    .withExternalId(externalId)
                    .withGatewayAccountId(gatewayAccountId)
                    .withType(ProductType.ADHOC)
                    .withPrice(1000)
                    .withProductPath(serviceNamePath, productNamePath)
                    .withLanguage(SupportedLanguage.WELSH)
                    .build()
                    .toProduct();

            app.getDatabaseTestHelper().addProduct(product);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products?serviceNamePath=%s&productNamePath=%s", serviceNamePath, productNamePath))
                    .then()
                    .statusCode(200);

            String urlToMatch = format("https://products-ui.url/products/%s/%s", serviceNamePath, productNamePath);
            response
                    .body("service_name_path", is(serviceNamePath))
                    .body("product_name_path", is(productNamePath))
                    .body("_links", hasSize(3))
                    .body("_links[2].href", is(urlToMatch))
                    .body("_links[2].method", is(HttpMethod.GET))
                    .body("_links[2].rel", is("friendly"))
                    .body(LANGUAGE, is("cy"));


        }

        @Test
         void findProductByProductPath_shouldReturn404_whenNotFound() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products?serviceNamePath=%s&productNamePath=%s", randomStringUtils.nextAlphanumeric(40), randomStringUtils.nextAlphanumeric(65)))
                    .then()
                    .statusCode(404);
        }

        @Test
         void findProductByProductPath_shouldReturn404_whenNullQueryParam() {
            app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get(format("/v1/api/products?serviceNamePath=%s&productNamePath=%s", RandomStringUtils.insecure().nextAlphanumeric(40), ""))
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    class ProductStats {
        @Test
         void shouldReturnUsageStats_whenProductAndPaymentsExist() {
            String nextUrl = "www.gov.uk/pay";

            String productExternalId = randomUuid();
            String productExternalId2 = randomUuid();
            String paymentExternalId1 = randomUuid();
            String paymentExternalId2 = randomUuid();

            ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(1)
                    .withExternalId(productExternalId)
                    .withType(ProductType.ADHOC)
                    .build();
            ProductEntity productEntity2 = ProductEntityFixture.aProductEntity()
                    .withGatewayAccountId(2)
                    .withExternalId(productExternalId2)
                    .withType(ProductType.ADHOC)
                    .build();

            Product product = productEntity.toProduct();
            Product product2 = productEntity2.toProduct();

            app.getDatabaseTestHelper().addProduct(product);
            app.getDatabaseTestHelper().addProduct(product2);

            productEntity.setId(app.getDatabaseTestHelper().findProductId(productExternalId));
            productEntity2.setId(app.getDatabaseTestHelper().findProductId(productExternalId2));

            PaymentEntity payment1 = aPaymentEntity()
                    .withExternalId(paymentExternalId1)
                    .withProduct(productEntity)
                    .withNextUrl(nextUrl)
                    .withReferenceNumber(paymentExternalId1.substring(0, 9))
                    .build();

            app.getDatabaseTestHelper().addPayment(payment1.toPayment(), 1);

            PaymentEntity payment2 = aPaymentEntity()
                    .withExternalId(paymentExternalId2)
                    .withProduct(productEntity2)
                    .withNextUrl(nextUrl)
                    .withReferenceNumber(paymentExternalId2.substring(0, 9))
                    .build();

            app.getDatabaseTestHelper().addPayment(payment2.toPayment(), 2);

            ValidatableResponse response = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .get("/v1/api/stats/products")
                    .then()
                    .statusCode(200);

            response
                    .body("size()", is(2))
                    .body("[0].payment_count", is(1))
                    .body("[0].product.external_id", is(product.getExternalId()))
                    .body("[1].payment_count", is(1))
                    .body("[1].product.external_id", is(product2.getExternalId()));

            ValidatableResponse filteredResponse = app.givenSetup()
                    .when()
                    .accept(APPLICATION_JSON)
                    .queryParam("gatewayAccountId", 1)
                    .get("/v1/api/stats/products")
                    .then()
                    .statusCode(200);
            filteredResponse
                    .body("size()", is(1))
                    .body("[0].product.external_id", is(product.getExternalId()));
        }
    }
}
