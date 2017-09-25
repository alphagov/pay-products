package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.ClassRule;
import uk.gov.pay.products.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.products.utils.DatabaseTestHelper;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;


public class IntegrationTest {

    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    protected DatabaseTestHelper databaseHelper;
    protected ObjectMapper mapper;

    @Before
    public void setUp() {
        databaseHelper = app.getDatabaseTestHelper();
        mapper = new ObjectMapper();
    }

    protected RequestSpecification givenSetup() {
        return given().port(app.getLocalPort())
                .contentType(JSON);
    }
}
