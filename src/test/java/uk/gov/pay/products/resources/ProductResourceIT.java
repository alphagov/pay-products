package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.commons.model.TokenPaymentType.CARD;
import static uk.gov.pay.products.client.publicauth.model.TokenSource.PRODUCTS;
import static uk.gov.pay.products.fixtures.PaymentEntityFixture.aPaymentEntity;
import static uk.gov.pay.products.service.ProductApiTokenManager.NEW_API_TOKEN_PATH;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductResourceIT extends IntegrationTest {

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
    private static final String LANGUAGE = "language";
    private static final String METADATA = "metadata";

    @Test
    public void shouldSuccess_whenSavingAValidProduct_withMinimumMandatoryFields() throws Exception {

        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;
        Integer gatewayAccountId = randomInt();
        String type = ProductType.DEMO.name();

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId)
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price)
                .put(TYPE, type)
                .put(RETURN_URL, "https://return.url")
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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
    public void shouldSuccess_whenSavingAValidProduct_withAllFieldsReferenceNotEnabled() throws Exception {

        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;
        String description = "Some test description";
        Integer gatewayAccountId = randomInt();
        String type = ProductType.ADHOC.name();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);
        String returnUrl = "https://some.valid.url";
        String language = "cy";

        ImmutableMap<String, String> payload = ImmutableMap.<String, String>builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString())
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price.toString())
                .put(DESCRIPTION, description)
                .put(TYPE, type)
                .put(RETURN_URL, returnUrl)
                .put(SERVICE_NAME_PATH, serviceNamePath)
                .put(PRODUCT_NAME_PATH, productNamePath)
                .put(REFERENCE_ENABLED_FIELD, Boolean.FALSE.toString())
                .put(LANGUAGE, language)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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
    public void shouldSuccess_whenSavingAValidProduct_withAllFieldsReferenceEnabled() throws Exception {

        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;
        String description = "Some test description";
        Integer gatewayAccountId = randomInt();
        String type = ProductType.ADHOC.name();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);
        String referenceLabel = randomAlphanumeric(25);
        String referenceHint = randomAlphanumeric(85);
        String returnUrl = "https://some.valid.url";
        String language = "en";

        ImmutableMap<String, String> payload = ImmutableMap.<String, String>builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString())
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price.toString())
                .put(DESCRIPTION, description)
                .put(TYPE, type)
                .put(RETURN_URL, returnUrl)
                .put(SERVICE_NAME_PATH, serviceNamePath)
                .put(PRODUCT_NAME_PATH, productNamePath)
                .put(REFERENCE_ENABLED_FIELD, Boolean.TRUE.toString())
                .put(REFERENCE_LABEL, referenceLabel)
                .put(REFERENCE_HINT, referenceHint)
                .put(LANGUAGE, language)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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
                .body("_links[1].href", matchesPattern(productsUIPayUrl + "reference/" + externalId))
                .body("_links[1].method", is(HttpMethod.GET))
                .body("_links[1].rel", is("pay"))
                .body("_links[2].href", is(urlToMatch))
                .body("_links[2].method", is(HttpMethod.GET))
                .body("_links[2].rel", is("friendly"));

    }

    @Test
    public void shouldError_whenSavingAProduct_withMandatoryFieldsMissing() throws Exception {
        givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString("{}"))
                .post("/v1/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldError_whenSavingAProduct_withProductPathAlreadyExisting() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withType(ProductType.ADHOC)
                .withPrice(1000)
                .withProductPath(serviceNamePath, productNamePath)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        String payApiToken = randomUuid();
        String name = "Existing product path product attempt";
        Long price = 1050L;
        String description = "Some test description";
        String type = ProductType.ADHOC.name();

        String returnUrl = "https://some.valid.url";

        ImmutableMap<String, String> payload = ImmutableMap.<String, String>builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString())
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price.toString())
                .put(DESCRIPTION, description)
                .put(TYPE, type)
                .put(RETURN_URL, returnUrl)
                .put(SERVICE_NAME_PATH, serviceNamePath)
                .put(PRODUCT_NAME_PATH, productNamePath)
                .build();

        givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(409);
    }

    @Test
    public void findProductByExternalId_shouldReturnProduct_whenFound() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        ValidatableResponse response = givenSetup()
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
                .body(RETURN_URL, is(product.getReturnUrl()));

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
    public void findProductByExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get("/v1/api/products/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void findProductByGatewayAccountIdAndExternalId_shouldReturnProduct_whenFound() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        ValidatableResponse response = givenSetup()
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
                .body(LANGUAGE, is("en"));

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
    public void findProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/gateway-account/%s/products/%s", randomInt(), randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void disableProductByExternalId_shouldReturn201_whenProductIsDisabled() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .patch(format("/v1/api/products/%s/disable", externalId))
                .then()
                .statusCode(204);

    }

    @Test
    public void disableProductByExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .patch(format("/v1/api/products/%s/disable", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void updateProduct_shouldUpdateProduct_whenFound_referenceEnabled() throws Exception {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        String updatedName = "updated-name";
        String updatedDescription = "updated-description";
        String updatedPrice = "1000";
        String updatedReferenceLabel = "updated-reference-label";
        String updatedReferenceHint = "updated-reference-hint";

        Product existingProduct = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withName("name")
                .withDescription("description")
                .withPrice(500)
                .withGatewayAccountId(gatewayAccountId)
                .withProductPath("service-name-path" + externalId, "product-name-path" + externalId)
                .build()
                .toProduct();

        databaseHelper.addProduct(existingProduct);

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, true)
                .put(REFERENCE_LABEL, updatedReferenceLabel)
                .put(REFERENCE_HINT, updatedReferenceHint)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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
                .body(TYPE, is(existingProduct.getType().name()))
                .body(GATEWAY_ACCOUNT_ID, is(gatewayAccountId))
                .body(RETURN_URL, is(existingProduct.getReturnUrl()))
                .body(LANGUAGE, is("en"));

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

        List<Map<String, Object>> productsRecords = databaseHelper.findProductEntityByGatewayAccountId(gatewayAccountId);
        Assert.assertThat(productsRecords.size(), is(1));
        Assert.assertThat(productsRecords.get(0), hasEntry("name", updatedName));
        Assert.assertThat(productsRecords.get(0), hasEntry("description", updatedDescription));
        Assert.assertThat(productsRecords.get(0), hasEntry("price", Long.valueOf(updatedPrice)));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_enabled", true));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_label", updatedReferenceLabel));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_hint", updatedReferenceHint));
    }

    @Test
    public void updateProduct_shouldUpdateProduct_whenFound_referenceEnabledNoHint() throws Exception {
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

        databaseHelper.addProduct(existingProduct);

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, true)
                .put(REFERENCE_LABEL, updatedReferenceLabel)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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

        List<Map<String, Object>> productsRecords = databaseHelper.findProductEntityByGatewayAccountId(gatewayAccountId);
        Assert.assertThat(productsRecords.size(), is(1));
        Assert.assertThat(productsRecords.get(0), hasEntry("name", updatedName));
        Assert.assertThat(productsRecords.get(0), hasEntry("description", updatedDescription));
        Assert.assertThat(productsRecords.get(0), hasEntry("price", Long.valueOf(updatedPrice)));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_enabled", true));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_label", updatedReferenceLabel));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_hint", null));
    }

    @Test
    public void updateProduct_shouldUpdateProduct_whenFound_referenceDisabled() throws Exception {
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

        databaseHelper.addProduct(existingProduct);

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, false)
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
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

        List<Map<String, Object>> productsRecords = databaseHelper.findProductEntityByGatewayAccountId(gatewayAccountId);
        Assert.assertThat(productsRecords.size(), is(1));
        Assert.assertThat(productsRecords.get(0), hasEntry("name", updatedName));
        Assert.assertThat(productsRecords.get(0), hasEntry("description", updatedDescription));
        Assert.assertThat(productsRecords.get(0), hasEntry("price", Long.valueOf(updatedPrice)));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_enabled", false));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_label", null));
        Assert.assertThat(productsRecords.get(0), hasEntry("reference_hint", null));
    }

    @Test
    public void updateProduct_shouldAddMetadataCorrectly() throws Exception {
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

        databaseHelper.addProduct(existingProduct);

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, false)
                .put(METADATA, Map.of("key1", "value1", "key3", "value3"))
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                .then()
                .statusCode(200);

        response
                .body(METADATA + ".key1", is("value1"))
                .body(METADATA + ".key3", is("value3"));

        List<Map<String, Object>> metadataByProductExternalId = databaseHelper.findMetadataByProductExternalId(externalId);
        assertThat(metadataByProductExternalId.size(), is(2));
        assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_key", "key1"));
        assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_value", "value1"));
        assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_key", "key3"));
        assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_value", "value3"));
    }

    @Test
    public void updateProduct_shouldReplaceExistingMetadataCorrectly() throws Exception {
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

        databaseHelper.addProduct(existingProduct);
        databaseHelper.addMetadata(externalId, "key1", "value1");
        databaseHelper.addMetadata(externalId, "key2", "value2");

        List<Map<String, Object>> metadataByProductExternalId = databaseHelper.findMetadataByProductExternalId(externalId);
        assertThat(metadataByProductExternalId.size(), is(2));

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, false)
                .put(METADATA, Map.of("key1", "new_value1", "key3", "value3"))
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                .then()
                .statusCode(200);

        response
                .body(METADATA + ".key1", is("new_value1"))
                .body(METADATA + ".key3", is("value3"));

        metadataByProductExternalId = databaseHelper.findMetadataByProductExternalId(externalId);
        assertThat(metadataByProductExternalId.size(), is(2));
        assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_key", "key1"));
        assertThat(metadataByProductExternalId.get(0), hasEntry("metadata_value", "new_value1"));
        assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_key", "key3"));
        assertThat(metadataByProductExternalId.get(1), hasEntry("metadata_value", "value3"));
    }

    @Test
    public void updateProduct_shouldReturn404_whenNotFound() throws Exception {
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

        databaseHelper.addProduct(existingProduct);

        ImmutableMap<String, Object> payload = ImmutableMap.<String, Object>builder()
                .put(NAME, updatedName)
                .put(PRICE, updatedPrice)
                .put(DESCRIPTION, updatedDescription)
                .put(REFERENCE_ENABLED_FIELD, false)
                .put(METADATA, Map.of("key1", "new_value1", "key3", "value3"))
                .build();

        givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch(format("/v1/api/gateway-account/%s/products/%s", anotherGatewayAccountId, externalId))
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteProductByExternalId_shouldReturn201_whenProductIsDeleted() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .delete(format("/v1/api/products/%s", externalId))
                .then()
                .statusCode(204);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s", externalId))
                .then()
                .statusCode(404);

    }

    @Test
    public void deleteProductByExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .delete(format("/v1/api/products/%s", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void deleteProductByGatewayAccountIdAndExternalId_shouldReturn201_whenProductIsDeleted() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .delete(format("/v1/api/gateway-account/%s/products/%s", gatewayAccountId, externalId))
                .then()
                .statusCode(204);
    }

    @Test
    public void deleteProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .delete(format("/v1/api/gateway-account/%s/products/%s", randomInt(), randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void disableProductByGatewayAccountIdAndExternalId_shouldReturn201_whenProductIsDisabled() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .patch(format("/v1/api/gateway-account/%s/products/%s/disable", gatewayAccountId, externalId))
                .then()
                .statusCode(204);

    }

    @Test
    public void disableProductByGatewayAccountIdAndExternalId_shouldReturn404_whenNotFound() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .patch(format("/v1/api/gateway-account/%s/products/%s/disable", randomInt(), randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void findProductsByGatewayAccountId_shouldReturnActiveProducts_whenFound() {
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Product product_2 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product_2);

        ValidatableResponse response = givenSetup()
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
    public void findProductsByGatewayAccountIdWithType_shouldReturnOnlyProductsWithMatchingType_whenFound() {
        int gatewayAccountId = randomInt();

        Product productWithCorrectType = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withType(ProductType.AGENT_INITIATED_MOTO)
                .build()
                .toProduct();

        databaseHelper.addProduct(productWithCorrectType);

        Product productWithIncorrectType = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct();

        databaseHelper.addProduct(productWithIncorrectType);

        ValidatableResponse response = givenSetup()
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
    public void findProductsByGatewayAccountId_shouldReturnNoProduct_whenNoneFound() {
        int unknownGatewayAccountId = randomInt();
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/gateway-account/%s/products", unknownGatewayAccountId))
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void findProductsByGatewayAccountIdWithType_shouldReturnNoProduct_whenNoneFound() {
        int unknownGatewayAccountId = randomInt();
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/gateway-account/%s/products?type=%s", unknownGatewayAccountId, ProductType.DEMO))
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void findProductsByGatewayAccountId_shouldNotReturnInactiveProducts() {
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withStatus(ProductStatus.INACTIVE)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/gateway-account/%s/products", gatewayAccountId))
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void findProductsByGatewayAccountIdWithType_shouldNotReturnInactiveProducts() {
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withStatus(ProductStatus.INACTIVE)
                .withType(ProductType.PROTOTYPE)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/gateway-account/%s/products?type=%s", gatewayAccountId, ProductType.PROTOTYPE))
                .then()
                .statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    public void findProductByProductPath_shouldReturnProduct_whenFound() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withType(ProductType.ADHOC)
                .withPrice(1000)
                .withProductPath(serviceNamePath, productNamePath)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        ValidatableResponse response = givenSetup()
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
    public void findProductByProductPath_shouldReturn404_whenNotFound() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products?serviceNamePath=%s&productNamePath=%s", randomAlphanumeric(40), randomAlphanumeric(65)))
                .then()
                .statusCode(404);
    }

    @Test
    public void findProductByProductPath_shouldReturn404_whenNullQueryParam() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products?serviceNamePath=%s&productNamePath=%s", randomAlphanumeric(40), ""))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldReturn400_whenSavingProduct_withReferenceEnabledAndNoReferenceLabel() throws Exception {

        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service";
        Long price = 1050L;
        String description = "Some test description";
        Integer gatewayAccountId = randomInt();
        String type = ProductType.ADHOC.name();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);
        String referenceHint = randomAlphanumeric(85);

        String returnUrl = "https://some.valid.url";

        ImmutableMap<String, String> payload = ImmutableMap.<String, String>builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId.toString())
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price.toString())
                .put(DESCRIPTION, description)
                .put(TYPE, type)
                .put(RETURN_URL, returnUrl)
                .put(SERVICE_NAME_PATH, serviceNamePath)
                .put(PRODUCT_NAME_PATH, productNamePath)
                .put(REFERENCE_ENABLED_FIELD, Boolean.TRUE.toString())
                .put(REFERENCE_HINT, referenceHint)
                .build();

        givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(400);
    }

    @Test
    public void shouldReturnMetadata_whenMetadataExistsForAProduct() {
        String externalId = randomUuid();
        int gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        databaseHelper.addMetadata(externalId, "key", "value");
        databaseHelper.addMetadata(externalId, "secondkey", "value2");

        ValidatableResponse response = givenSetup()
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

    @Test
    public void shouldReturnUsageStats_whenProductAndPaymensExist() {
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

        databaseHelper.addProduct(product);
        databaseHelper.addProduct(product2);

        productEntity.setId(databaseHelper.findProductId(productExternalId));
        productEntity2.setId(databaseHelper.findProductId(productExternalId2));

        PaymentEntity payment1 = aPaymentEntity()
                .withExternalId(paymentExternalId1)
                .withProduct(productEntity)
                .withNextUrl(nextUrl)
                .withReferenceNumber(paymentExternalId1.substring(0, 9))
                .build();

        databaseHelper.addPayment(payment1.toPayment(), 1);

        PaymentEntity payment2 = aPaymentEntity()
                .withExternalId(paymentExternalId2)
                .withProduct(productEntity2)
                .withNextUrl(nextUrl)
                .withReferenceNumber(paymentExternalId2.substring(0, 9))
                .build();

        databaseHelper.addPayment(payment2.toPayment(), 2);

        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/stats/products"))
                .then()
                .statusCode(200);

        response
                .body("size", is(2))
                .body("[0].payment_count", is(1))
                .body("[0].product.external_id", is(product.getExternalId()))
                .body("[1].payment_count", is(1))
                .body("[1].product.external_id", is(product2.getExternalId()));

        ValidatableResponse filteredResponse = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .queryParam("gatewayAccountId", 1)
                .get(format("/v1/api/stats/products"))
                .then()
                .statusCode(200);
        filteredResponse
                .body("size", is(1))
                .body("[0].product.external_id", is(product.getExternalId()));
    }

    @Test
    public void shouldReturn200_whenApiTokenIsUpdatedForAProduct() {
        publicAuthRule.resetAll();
        final Product product = ProductEntityFixture.aProductEntity().build().toProduct();
        databaseHelper.addProduct(product);
        final String newApiToken = "New API token";
        setUpPublicAuthStubForGeneratingApiToken(product, newApiToken);

        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s", product.getExternalId()))
                .then()
                .statusCode(200);

        String payApiToken = response.extract().path(PAY_API_TOKEN);
        assertThat(payApiToken, is(product.getPayApiToken()));

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/regenerate-api-token", product.getExternalId()))
                .then()
                .statusCode(200);

        response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s", product.getExternalId()))
                .then()
                .statusCode(200);

        payApiToken = response.extract().path(PAY_API_TOKEN);
        assertThat(payApiToken, is(newApiToken));
    }

    @Test
    public void shouldReturn500_whenAnApiTokenFailsToBeGeneratedBeforeUpdatingApiToken() {
        publicAuthRule.resetAll();
        final Product product = ProductEntityFixture.aProductEntity().build().toProduct();
        databaseHelper.addProduct(product);
        setUpPublicAuthStubForFailingToGenerateApiToken(product);

        ValidatableResponse response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s", product.getExternalId()))
                .then()
                .statusCode(200);

        String payApiToken = response.extract().path(PAY_API_TOKEN);
        assertThat(payApiToken, is(product.getPayApiToken()));

        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/regenerate-api-token", product.getExternalId()))
                .then()
                .statusCode(500);

        response = givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .get(format("/v1/api/products/%s", product.getExternalId()))
                .then()
                .statusCode(200);

        payApiToken = response.extract().path(PAY_API_TOKEN);
        assertThat(payApiToken, is(product.getPayApiToken()));
    }

    @Test
    public void shouldReturn404_whenAProductDoesNotExistBeforeUpdatingApiToken() {
        givenSetup()
                .when()
                .accept(APPLICATION_JSON)
                .post(format("/v1/api/products/%s/regenerate-api-token", randomUuid()))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldSuccess_whenSavingAValidProduct_withMetadataFields() throws Exception {

        String payApiToken = randomUuid();
        String name = "Flashy new GOV Service with metadata";
        Long price = 1050L;
        Integer gatewayAccountId = randomInt();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);
        String type = ProductType.ADHOC.name();

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(GATEWAY_ACCOUNT_ID, gatewayAccountId)
                .put(PAY_API_TOKEN, payApiToken)
                .put(NAME, name)
                .put(PRICE, price)
                .put(TYPE, type)
                .put(SERVICE_NAME_PATH, serviceNamePath)
                .put(PRODUCT_NAME_PATH, productNamePath)
                .put(METADATA, Map.of("key1", "value1", "key2", "value2"))
                .put(RETURN_URL, "https://return.url")
                .build();

        ValidatableResponse response = givenSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(201);

        String externalId = response.extract().path(EXTERNAL_ID);

        List<Map<String, Object>> metadata = databaseHelper.findMetadataByProductExternalId(externalId);

        assertThat(metadata.size(), is(2));
        assertThat(metadata.get(0), hasEntry("metadata_key", "key1"));
        assertThat(metadata.get(0), hasEntry("metadata_value", "value1"));
        assertThat(metadata.get(1), hasEntry("metadata_key", "key2"));
        assertThat(metadata.get(1), hasEntry("metadata_value", "value2"));
    }

    private void setUpPublicAuthStubForGeneratingApiToken(Product product, String newApiToken) {
        JsonObject createApiTokenRequest = Json.createObjectBuilder()
                .add("description", "Token for Demo Payment")
                .add("account_id", product.getGatewayAccountId().toString())
                .add("created_by", app.getConfiguration().getEmailAddressForReplacingApiTokens())
                .add("token_type", CARD.toString())
                .add("type", PRODUCTS.toString())
                .build();

        JsonObject newPayApiToken = Json.createObjectBuilder()
                .add("token", newApiToken)
                .build();

        publicAuthRule.stubFor(post(urlEqualTo(NEW_API_TOKEN_PATH))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON))
                .withRequestBody(equalToJson(createApiTokenRequest.toString(), true, true))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(Response.Status.OK.getStatusCode())
                        .withBody(newPayApiToken.toString())));
    }

    private void setUpPublicAuthStubForFailingToGenerateApiToken(Product product) {
        JsonObject createApiTokenRequest = Json.createObjectBuilder()
                .add("description", "Token for Demo Payment")
                .add("account_id", product.getGatewayAccountId().toString())
                .add("created_by", app.getConfiguration().getEmailAddressForReplacingApiTokens())
                .add("token_type", CARD.toString())
                .add("type", PRODUCTS.toString())
                .build();

        publicAuthRule.stubFor(post(urlEqualTo(NEW_API_TOKEN_PATH))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON))
                .withRequestBody(equalToJson(createApiTokenRequest.toString(), true, true))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())));
    }
}
