package uk.gov.pay.products.pact;

import au.com.dius.pact.provider.junit.State;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junit.target.Target;
import au.com.dius.pact.provider.junit.target.TestTarget;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import uk.gov.pay.products.infra.DropwizardAppWithPostgresRule;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.ProductType;
import uk.gov.pay.products.utils.DatabaseTestHelper;

import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;

public abstract class ContractTest {
    @ClassRule
    public static DropwizardAppWithPostgresRule app = new DropwizardAppWithPostgresRule();

    @TestTarget
    public static Target target;

    private static DatabaseTestHelper dbHelper;

    @BeforeClass
    public static void setUpService() {
        target = new HttpTarget(app.getLocalPort());
        dbHelper = app.getDatabaseTestHelper();
    }

    @Before
    public void resetDatabase() {
        dbHelper.truncateAllData();
    }

    @State("default")
    public void noSetUp() {
    }

    @State({"a product with external id existing-id exists",
            "a product with external id existing-id and gateway account id 42 exists"})
    public void aProductExists() {
        Product product = aProductEntity()
                .withExternalId("existing-id")
                .withGatewayAccountId(42)
                .build()
                .toProduct();

        dbHelper.addProduct(product);
    }

    @State("a product with path service-name-path/product-name-path exists")
    public void aProductWithPathExists() {
        Product product = aProductEntity()
                .withProductPath("service-name-path", "product-name-path")
                .build()
                .toProduct();

        dbHelper.addProduct(product);
    }

    @State("three products with gateway account id 42 exist")
    public void threeProductsExistForGatewayAccount() {
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .build()
                .toProduct());
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .build()
                .toProduct());
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .build()
                .toProduct());
    }

    @State("three products with gateway account id 42 and type ADHOC exist")
    public void threeProductsExistForGatewayAccountAndTypeAdhoc() {
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct());
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct());
        dbHelper.addProduct(aProductEntity()
                .withGatewayAccountId(42)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct());
    }

    @State("a product with gateway account id 42 and metadata exist")
    public void aProductsWithMetadataExistForGatewayAccount() {
        Product productWithMetadata = aProductEntity()
                .withGatewayAccountId(42)
                .build()
                .toProduct();
        dbHelper.addProduct(productWithMetadata);
        dbHelper.addMetadata(productWithMetadata.getExternalId(), "key", "value");
    }

    @State("a product with gateway account id 42 and type ADHOC and metadata exist")
    public void aProductsWithMetadataExistForGatewayAccountAndType() {
        Product productWithMetadata = aProductEntity()
                .withGatewayAccountId(42)
                .withType(ProductType.ADHOC)
                .build()
                .toProduct();
        dbHelper.addProduct(productWithMetadata);
        dbHelper.addMetadata(productWithMetadata.getExternalId(), "key", "value");
    }
}
