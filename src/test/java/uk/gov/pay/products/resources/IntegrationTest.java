package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import uk.gov.pay.products.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.utils.DatabaseTestHelper;
import uk.gov.service.payments.commons.testing.port.PortFactory;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class IntegrationTest {

    public final static int PUBLIC_API_PORT = PortFactory.findFreePort();
    public final static int PUBLIC_AUTH_PORT = PortFactory.findFreePort();
    
    @ClassRule
    public static final WireMockRule publicApiRule = new WireMockRule(PUBLIC_API_PORT);

    @ClassRule
    public static final DropwizardAppWithPostgresRule app =
            new DropwizardAppWithPostgresRule("config/test-it-config.yaml",
                    config("publicApiUrl", "http://localhost:" + PUBLIC_API_PORT));

    protected static DatabaseTestHelper databaseHelper;
    protected static ObjectMapper mapper;

    @BeforeClass
    public static void setUp() {
        databaseHelper = app.getDatabaseTestHelper();
        mapper = new ObjectMapper();
    }

    RequestSpecification givenSetup() {
        return given().port(app.getLocalPort())
                .contentType(JSON);
    }

    protected PaymentEntity addPaymentToDB(PaymentEntity paymentEntity) {
        databaseHelper.addPayment(paymentEntity.toPayment(), paymentEntity.getGatewayAccountId());
        return paymentEntity;
    }
    
    protected ProductEntity addProductToDB(ProductEntity productEntity) {
        Product product = productEntity.toProduct();
        databaseHelper.addProduct(product);
        Integer productId = databaseHelper.findProductId(productEntity.getExternalId());
        productEntity.setId(productId);
        return productEntity;
    }
}
