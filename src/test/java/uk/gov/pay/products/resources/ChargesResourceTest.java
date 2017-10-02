package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.products.fixtures.CatalogueEntityFixture.aCatalogueEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ChargesResourceTest extends IntegrationTest {
    private static final String EXTERNAL_SERVICE_ID = "external_service_id";
    private static final String EXTERNAL_PRODUCT_ID = "external_product_id";
    private static final String NAME = "name";
    private static final String CHARGES_PATH = "/v1/api/charges";

    @Test
    public void givenAnExternalProductId_shouldCreateANewCharge() throws Exception {
        String externalId = randomUuid();
        CatalogueEntity aCatalogueEntity = aCatalogueEntity().build();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withCatalogue(aCatalogueEntity)
                .build()
                .toProduct();

        int catalogueId = randomInt();
        databaseHelper.addProduct(product, catalogueId);

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(EXTERNAL_PRODUCT_ID, product.getExternalId())
                .build();

        ValidatableResponse response = givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(CHARGES_PATH)
                .then()
                .statusCode(201);

//        response
//                .body(NAME, is(name))
//                .body(EXTERNAL_SERVICE_ID, is(externalServiceId));

    }

    @Test
    public void givenEmptyPayload_whenCreateNewCharge_shouldReturn400() throws Exception {
        givenAuthenticatedSetup()
                .accept(APPLICATION_JSON)
                .post(CHARGES_PATH)
                .then()
                .statusCode(400);
    }

    @Test
    public void givenANotAuthenticatedRequest_whenCreateNewCharge_shouldReturn401() throws Exception {
        givenSetup()
                .accept(APPLICATION_JSON)
                .post(CHARGES_PATH)
                .then()
                .statusCode(401);
    }
}
