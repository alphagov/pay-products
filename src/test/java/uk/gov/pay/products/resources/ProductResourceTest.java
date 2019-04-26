package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import javax.ws.rs.HttpMethod;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductResourceTest extends IntegrationTest {

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
                .withProductPath("service-name-path" + externalId, "product-name-path"+ externalId)
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
                .withProductPath("service-name-path" + externalId, "product-name-path"+ externalId)
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
                .withProductPath("service-name-path" + externalId, "product-name-path"+ externalId)
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
}
