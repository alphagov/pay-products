package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.products.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.products.utils.DatabaseTestHelper;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.http.Headers.headers;

public class IntegrationTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    @Rule
    public DropwizardAppWithPostgresRule app =
            new DropwizardAppWithPostgresRule("config/test-it-config.yaml",
                    config("publicApiUrl", "http://localhost:" + mockServerRule.getPort()));

    protected DatabaseTestHelper databaseHelper;
    protected ObjectMapper mapper;
    protected Header validAuthHeader;

    protected static final String DEFAULT_AUTH_TOKEN = "default-local-api-token";

    @Before
    public void setUp() {
        databaseHelper = app.getDatabaseTestHelper();
        mapper = new ObjectMapper();
        validAuthHeader = new Header("Authorization", "Bearer " + DEFAULT_AUTH_TOKEN);
    }

    protected RequestSpecification givenAuthenticatedSetup() {
        return given().port(app.getLocalPort())
                .contentType(JSON)
                .headers(headers(validAuthHeader));
    }

    protected RequestSpecification givenSetup() {
        return given().port(app.getLocalPort())
                .contentType(JSON);
    }
}
