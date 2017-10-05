package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;
import static uk.gov.pay.products.fixtures.CatalogueEntityFixture.aCatalogueEntity;
import static uk.gov.pay.products.util.ChargeJsonField.*;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ChargesResourceTest extends IntegrationTest {
    private static final String CHARGES_PATH = "/v1/api/charges";

    @Test
    public void givenAnExternalProductId_shouldCreateANewCharge() throws Exception {
        String externalId = randomUuid();
        Long price = 559L;
        String description = "Test description";

        CatalogueEntity aCatalogueEntity = aCatalogueEntity().build();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withCatalogue(aCatalogueEntity)
                .withPrice(price)
                .withDescription(description)
                .build()
                .toProduct();

        int catalogueId = randomInt();
        databaseHelper.addProductAndCatalogue(product, catalogueId);

        ImmutableMap<Object, Object> payload = ImmutableMap.builder()
                .put(PRODUCT_EXTERNAL_ID, product.getExternalId())
                .build();

        ValidatableResponse response = givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(CHARGES_PATH)
                .then()
                .statusCode(200);

        response
                .body(CHARGE_EXTERNAL_ID, is(not(isEmptyString())))
                .body(PRODUCT_EXTERNAL_ID, is(externalId))
                .body(AMOUNT, is(559));
    }

    @Test
    public void givenEmptyPayload_whenCreateNewCharge_shouldReturn400() throws Exception {
        givenAuthenticatedSetup()
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString("{}"))
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
