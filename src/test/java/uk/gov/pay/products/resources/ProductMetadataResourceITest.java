package uk.gov.pay.products.resources;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.products.model.Product;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductMetadataResourceITest extends IntegrationTest {

    @Test
    public void addNewMetadataShouldSucceed() throws Exception {
        String productExternalId = randomUuid();
        Product product = aProductEntity()
                .withExternalId(productExternalId)
                .withGatewayAccountId(0)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Map<String, String> payload = ImmutableMap.of("Location", "London");
        givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/metadata", product.getExternalId()))
                .then()
                .statusCode(201);

        List<Map<String, Object>> metadata = databaseHelper.findMetadataByProductExternalId(productExternalId);
        assertThat(metadata.get(0), hasEntry("metadata_key", "Location"));
        assertThat(metadata.get(0), hasEntry("metadata_value", "London"));
    }

    @Test
    public void addNewMetadata_shouldFail_whenKeyExists() throws Exception {
        String productExternalId = randomUuid();
        Product product = aProductEntity()
                .withExternalId(productExternalId)
                .withGatewayAccountId(0)
                .withLanguage(SupportedLanguage.WELSH)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);
        databaseHelper.addMetadata(productExternalId, "Location", "Madrid");

        Map<String, String> payload = ImmutableMap.of("location", "London");
        givenSetup()
                .accept(APPLICATION_JSON)
                .body(mapper.writeValueAsString(payload))
                .post(format("/v1/api/products/%s/metadata", product.getExternalId()))
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("errors[0]", is(format("Key [ %s ] already exists, duplicate keys not allowed", "location")));;
    }
}