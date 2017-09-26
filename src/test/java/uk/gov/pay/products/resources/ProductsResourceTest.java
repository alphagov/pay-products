package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.Serializable;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductsResourceTest extends IntegrationTest {


    @Test
    public void shouldSuccess_whenSavingAValidProduct_withMinimumMandatoryFields() throws Exception {

        String apiToken = randomUuid();
        ImmutableMap<String, ? extends Serializable> payload = ImmutableMap.of("external_service_id", randomUuid(), "pay_api_token", apiToken, "name", "Flashy new GOV Service", "price", 1050);

        givenSetup()
                .contentType(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post("/v1/api/products")
                .then()
                .statusCode(201)
                .body("name", is("Flashy new GOV Service"))
                .body("pay_api_token", is(apiToken))
                .body("price", is(1050))
                .body("return_url", matchesPattern("^http://(.*)/products/[0-9a-z]{32}$/confirmation"));

    }
}
