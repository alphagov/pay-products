package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.products.model.PatchRequest.FIELD_OPERATION;
import static uk.gov.pay.products.model.PatchRequest.FIELD_OPERATION_PATH;
import static uk.gov.pay.products.model.PatchRequest.FIELD_VALUE;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;

public class GatewayAccountResourceTest extends IntegrationTest {

    private static final String OP = "update";
    private static final String PATH = "service_name";
    private static final String VALUE = "A New Name";

    @Test
    public void shouldUpdateServiceName_withValidMandatoryFields() throws Exception {
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withServiceName("Old Service Name")
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(FIELD_OPERATION, OP)
                .put(FIELD_OPERATION_PATH, PATH)
                .put(FIELD_VALUE, VALUE)
                .build();

        givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch("/v1/api/gateway-account/" + gatewayAccountId)
                .then()
                .statusCode(200);

        List<Map<String, Object>> productsRecords = databaseHelper.findProductEntityByGatewayAccountId(gatewayAccountId);

        assertThat(productsRecords.get(0), hasEntry("service_name", VALUE));
    }

    @Test
    public void shouldUpdateMultipleRecordsServiceName_withValidMandatoryFields() throws Exception {
        Integer gatewayAccountId = randomInt();

        Product product1 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withServiceName("Old Service Name")
                .build()
                .toProduct();

        Product product2 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withServiceName("Old Service Name")
                .build()
                .toProduct();

        databaseHelper.addProduct(product1);
        databaseHelper.addProduct(product2);

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(FIELD_OPERATION, OP)
                .put(FIELD_OPERATION_PATH, PATH)
                .put(FIELD_VALUE, VALUE)
                .build();

        givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch("/v1/api/gateway-account/" + gatewayAccountId)
                .then()
                .statusCode(200);

        List<Map<String, Object>> productsRecords = databaseHelper.findProductEntityByGatewayAccountId(gatewayAccountId);

        assertThat(productsRecords.get(0), hasEntry("service_name", VALUE));
        assertThat(productsRecords.get(1), hasEntry("service_name", VALUE));
    }

    @Test
    public void updateAProduct_shouldFail_whenNotAuthenticated() throws Exception {
        givenSetup()
                .accept(APPLICATION_JSON)
                .patch(format("/v1/api/gateway-account/%s", randomInt()))
                .then()
                .statusCode(401);
    }

    @Test
    public void givenANonExistingProduct_whenUpdateAServiceName_shouldReturn404() throws Exception {
        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(FIELD_OPERATION, OP)
                .put(FIELD_OPERATION_PATH, PATH)
                .put(FIELD_VALUE, VALUE)
                .build();

        givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .patch(format("/v1/api/gateway-account/%s", randomInt()))
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldError_whenUpdatingAServiceName_withMandatoryFieldsMissing() throws Exception {
        givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString("{}"))
                .patch(format("/v1/api/gateway-account/%s", randomInt()))
                .then()
                .statusCode(400);
    }
}
