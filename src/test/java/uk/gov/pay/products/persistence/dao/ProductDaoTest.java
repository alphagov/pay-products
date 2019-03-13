package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.matchers.ProductMatcher;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.util.RandomIdGenerator.randomInt;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductDaoTest extends DaoTestBase {

    private ProductDao productDao;

    @Before
    public void before() {
        productDao = env.getInstance(ProductDao.class);
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

        databaseHelper.addProduct(product);

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

        databaseHelper.addProduct(product);

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

        databaseHelper.addProduct(product);

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

        databaseHelper.addProduct(product);

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

        databaseHelper.addProduct(activeProduct);

        Product inactiveProduct = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withStatus(ProductStatus.INACTIVE)
                .build()
                .toProduct();

        databaseHelper.addProduct(inactiveProduct);

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

        productDao.persist(product);

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

        databaseHelper.addProduct(product);

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

        databaseHelper.addProduct(product);

        Optional<ProductEntity> productEntity = productDao.findByProductPath(serviceNamePath, anotherProductNamePath);
       assertThat(productEntity.isPresent(), is(false));
    }
}
