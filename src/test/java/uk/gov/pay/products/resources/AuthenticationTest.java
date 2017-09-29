package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.Serializable;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class AuthenticationTest extends IntegrationTest {

    @Test
    public void shouldFail_whenSavingAProduct_withInCorrectAuthToken() throws Exception {
        ImmutableMap<String, ? extends Serializable> payload = ImmutableMap.of(
                "external_service_id", randomUuid(),
                "pay_api_token", randomUuid(),
                "name", "a-name",
                "price", 1234);

        givenSetup()
                .contentType(APPLICATION_JSON)
                .header("Authorization", "Bearer invalid-api-key")
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(401);

    }

    @Test
    public void shouldSuccess_whenSavingAProduct_withCorrectAuthToken() throws Exception {
        ImmutableMap<String, ? extends Serializable> payload = ImmutableMap.of(
                "external_service_id", randomUuid(),
                "pay_api_token", randomUuid(),
                "name", "a-name",
                "price", 1234);

        givenSetup()
                .contentType(APPLICATION_JSON)
                .header(validAuthHeader)
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(201);

    }
}
