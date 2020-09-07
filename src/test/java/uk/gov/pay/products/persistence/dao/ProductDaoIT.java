package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.PaymentEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.fixtures.ProductMetadataEntityFixture;
import uk.gov.pay.products.matchers.ProductMatcher;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductUsageStat;
import uk.gov.pay.products.persistence.entity.PaymentEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;
import uk.gov.pay.products.util.PaymentStatus;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductDaoIT extends DaoTestBase {

    private ProductDao productDao;
    private ProductMetadataDao productMetadataDao;

    @Before
    public void before() {
        System.out.println("ProductDaoIT before");
        productDao = env.getInstance(ProductDao.class);
        productMetadataDao = env.getInstance(ProductMetadataDao.class);
    }

    @Test
    public void findByExternalId_shouldReturnAProduct_whenExists() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        System.out.println("findByExternalId_shouldReturnAProduct_whenExists add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByExternalId_shouldReturnAProduct_whenExists find productEntity by ExternalID");
        Optional<ProductEntity> productEntity = productDao.findByExternalId(externalId);
        assertTrue(productEntity.isPresent());
        assertThat(productEntity.get().toProduct(), ProductMatcher.isSame(product));
    }

    @Test
    public void findByExternalId_shouldNotReturnAProduct_whenDoesNotExist() {
        String externalId = "xxx";
        String anotherExternalId = "yyy";
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        System.out.println("findByExternalId_shouldNotReturnAProduct_whenDoesNotExist add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByExternalId_shouldNotReturnAProduct_whenDoesNotExist find productEntity by ExternalID");
        Optional<ProductEntity> productEntity = productDao.findByExternalId(anotherExternalId);
        assertFalse(productEntity.isPresent());
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnAProduct_whenExists() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        System.out.println("findByGatewayAccountIdAndExternalId_shouldReturnAProduct_whenExists add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByGatewayAccountIdAndExternalId_shouldReturnAProduct_whenExists find ProductEntity via GWA ID and External ID");
        Optional<ProductEntity> productEntity = productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId);
        assertTrue(productEntity.isPresent());
        assertThat(productEntity.get().toProduct(), ProductMatcher.isSame(product));
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldNotReturnAProduct_whenDoesNotExist() {
        String externalId = randomUuid();
        Integer gatewayAccountId = 0;
        Integer anotherGatewayAccountId = 1;

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        System.out.println("findByGatewayAccountIdAndExternalId_shouldNotReturnAProduct_whenDoesNotExist add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByGatewayAccountIdAndExternalId_shouldNotReturnAProduct_whenDoesNotExist find ProductEntity via GWA ID and External ID");
        Optional<ProductEntity> productEntity = productDao.findByGatewayAccountIdAndExternalId(anotherGatewayAccountId, externalId);
        assertFalse(productEntity.isPresent());

    }

    @Test
    public void findByGatewayAccountId_shouldReturnActiveProductsForTheGivenAccount() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        Product activeProduct = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        System.out.println("findByGatewayAccountId_shouldReturnActiveProductsForTheGivenAccount add product via DB Helper");
        databaseHelper.addProduct(activeProduct);

        Product inactiveProduct = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withStatus(ProductStatus.INACTIVE)
                .build()
                .toProduct();

        System.out.println("findByGatewayAccountId_shouldReturnActiveProductsForTheGivenAccount add product via DB Helper");
        databaseHelper.addProduct(inactiveProduct);

        System.out.println("findByGatewayAccountId_shouldReturnActiveProductsForTheGivenAccount find product via GWA ID");
        List<ProductEntity> products = productDao.findByGatewayAccountId(gatewayAccountId);
        assertThat(products.size(), is(1));
        assertThat(products.get(0).toProduct(), ProductMatcher.isSame(activeProduct));
    }

    @Test
    public void persist_shouldSucceed_whenTheProductIsValid() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withName("test name")
                .build();

        System.out.println("persist_shouldSucceed_whenTheProductIsValid persist product");
        productDao.persist(product);

        System.out.println("persist_shouldSucceed_whenTheProductIsValid find product by ExternalID");
        Optional<ProductEntity> newProduct = productDao.findByExternalId(externalId);
        assertTrue(newProduct.isPresent());
        assertThat(newProduct.get().toProduct(), ProductMatcher.isSame(product.toProduct()));
    }

    @Test
    public void findByProductPath_shouldReturnAProduct_whenExists() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withProductPath(serviceNamePath, productNamePath)
                .build()
                .toProduct();

        System.out.println("findByProductPath_shouldReturnAProduct_whenExists add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByProductPath_shouldReturnAProduct_whenExists find product by ProductPath");
        Optional<ProductEntity> productEntity = productDao.findByProductPath(serviceNamePath, productNamePath);
        assertTrue(productEntity.isPresent());
        assertThat(productEntity.get().toProduct(), ProductMatcher.isSame(product));
    }

    @Test
    public void findByProductPath_shouldNotReturnAProduct_whenDoesNotExists() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();
        String serviceNamePath = randomAlphanumeric(40);
        String productNamePath = randomAlphanumeric(65);
        String anotherProductNamePath = randomAlphanumeric(15);

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withProductPath(serviceNamePath, productNamePath)
                .build()
                .toProduct();

        System.out.println("findByProductPath_shouldNotReturnAProduct_whenDoesNotExists add product via DB Helper");
        databaseHelper.addProduct(product);

        System.out.println("findByProductPath_shouldNotReturnAProduct_whenDoesNotExists find product by ProductPath");
        Optional<ProductEntity> productEntity = productDao.findByProductPath(serviceNamePath, anotherProductNamePath);
       assertThat(productEntity.isPresent(), is(false));
    }

    @Test
    public void findById_shouldReturnMetadata_whenItExistsForAPaymentLink() {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withName("test name")
                .build();

        System.out.println("findById_shouldReturnMetadata_whenItExistsForAPaymentLink add product via DB Helper");
        databaseHelper.addProduct(product.toProduct());

        System.out.println("findById_shouldReturnMetadata_whenItExistsForAPaymentLink find product by externalID");
        Optional<ProductEntity> productWithId = productDao.findByExternalId(externalId);

        ProductMetadataEntity productMetadataEntity = ProductMetadataEntityFixture.aProductMetadataEntity()
                .withProductEntity(productWithId.get())
                .withMetadataValue("value1")
                .withMetadataKey("key1")
                .build();
        System.out.println("findById_shouldReturnMetadata_whenItExistsForAPaymentLink merge product");
        productMetadataDao.merge(productMetadataEntity);

        ProductMetadataEntity productMetadataEntity2 = ProductMetadataEntityFixture.aProductMetadataEntity()
                .withProductEntity(productWithId.get())
                .withMetadataValue("value2")
                .withMetadataKey("key2")
                .build();
        System.out.println("findById_shouldReturnMetadata_whenItExistsForAPaymentLink merge product");
        productMetadataDao.merge(productMetadataEntity2);

        System.out.println("findById_shouldReturnMetadata_whenItExistsForAPaymentLink find product by externalID");
        Optional<ProductEntity> newProduct = productDao.findByExternalId(externalId);
        assertThat(newProduct.get().getMetadataEntityList().size(), is(2));

        Map<String, String> productMetadataMap = newProduct.get().toProductMetadataMap();
        assertThat(productMetadataMap.size(), is(2));
        assertThat(productMetadataMap.containsKey("key1"), is(true));
        assertThat(productMetadataMap.containsValue("value1"), is(true));
        assertThat(productMetadataMap.containsKey("key2"), is(true));
        assertThat(productMetadataMap.containsValue("value2"), is(true));
    }

    @Test
    public void findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc() {
        ZonedDateTime now = ZonedDateTime.parse("2020-04-01T12:05:05.073Z");
        ProductEntity productEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(randomUuid())
                .withType(ProductType.ADHOC)
                .withGatewayAccountId(1)
                .build();
        ProductEntity secondProductEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(randomUuid())
                .withType(ProductType.ADHOC)
                .withGatewayAccountId(2)
                .build();
        ProductEntity ignoredProductEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(randomUuid())
                .withType(ProductType.DEMO)
                .withGatewayAccountId(1)
                .build();

        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc merge product");
        productEntity = productDao.merge(productEntity);
        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc merge product");
        secondProductEntity = productDao.merge(secondProductEntity);
        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc merge product");
        ignoredProductEntity = productDao.merge(ignoredProductEntity);

        PaymentEntity payment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .withReferenceNumber("MH2KJY5KPW")
                .withDateCreated(now)
                .build();
        PaymentEntity secondPayment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(productEntity)
                .withReferenceNumber("MH2KJY5KPW")
                .withDateCreated(now.minus(2, ChronoUnit.DAYS))
                .build();
        PaymentEntity thirdPayment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(secondProductEntity)
                .withReferenceNumber("MH2KJY5KPW")
                .build();
        PaymentEntity ignoredPayment = PaymentEntityFixture.aPaymentEntity()
                .withExternalId(randomUuid())
                .withStatus(PaymentStatus.CREATED)
                .withProduct(ignoredProductEntity)
                .withReferenceNumber("MH2KJY5KPW")
                .build();

        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc add payment via dbhelper");
        databaseHelper.addPayment(payment.toPayment(), 1);
        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc add payment via dbhelper");
        databaseHelper.addPayment(secondPayment.toPayment(), 1);
        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc add payment via dbhelper");
        databaseHelper.addPayment(thirdPayment.toPayment(), 1);
        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc add payment via dbhelper");
        databaseHelper.addPayment(ignoredPayment.toPayment(), 1);

        System.out.println("findProductsAndUsage_shouldReturnAProductUsage_whenExistsWithTypeAdhoc find Products and Usage");
        List<ProductUsageStat> usageStats = productDao.findProductsAndUsage(null);
        List<ProductUsageStat> filteredUsageStats = productDao.findProductsAndUsage(2);

        // product with type of demo is ignored resulting in only two products reported on
        assertThat(usageStats.size(), is(2));
        assertThat(usageStats.get(0).getPaymentCount(), is(2L));
        assertThat(usageStats.get(1).getPaymentCount(), is(1L));
 
        assertThat(filteredUsageStats.size(), is(1));
        assertThat(filteredUsageStats.get(0).getPaymentCount(), is(1L));
        assertThat(filteredUsageStats.get(0).getProduct().getExternalId(), is(secondProductEntity.getExternalId()));
    }
}
