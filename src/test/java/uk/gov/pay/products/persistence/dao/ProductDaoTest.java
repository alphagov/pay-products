package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.matchers.ProductMatcher;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
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
    public void findByExternalId_shouldReturnAProduct_whenExists() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Optional<ProductEntity> productEntity = productDao.findByFriendlyUrlOrExternalId(externalId);
        assertTrue(productEntity.isPresent());
        assertThat(productEntity.get().toProduct(), ProductMatcher.isSame(product));
    }

    @Test
    public void findByExternalId_shouldNotReturnAProduct_whenDoesNotExist() throws Exception {
        String externalId = randomUuid();
        String anotherExternalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Optional<ProductEntity> productEntity = productDao.findByFriendlyUrlOrExternalId(anotherExternalId);
        assertFalse(productEntity.isPresent());
    }

    @Test
    public void findByGatewayAccountIdAndExternalId_shouldReturnAProduct_whenExists() throws Exception {
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
    public void findByGatewayAccountIdAndExternalId_shouldNotReturnAProduct_whenDoesNotExist() throws Exception {
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
    public void findByGatewayAccountId_shouldReturnActiveProductsForTheGivenAccount() throws Exception {
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
    public void persist_shouldSucceed_whenTheProductIsValid() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withName("test name")
                .build();

        productDao.persist(product);

        Optional<ProductEntity> newProduct = productDao.findByFriendlyUrlOrExternalId(externalId);
        assertTrue(newProduct.isPresent());
        assertThat(newProduct.get().toProduct(), ProductMatcher.isSame(product.toProduct()));
    }

    @Test
    public void updateGatewayAccount_shouldUpdateTheServiceNameOfAllProductOfGivenGatewayAccount() throws Exception {
        Integer gatewayAccountId = randomInt();
        Integer anotherGatewayAccountId = randomInt();
        String oldServiceName = "Old Service Name";
        String newServiceName = "New Service Name";

        Product product1 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withServiceName(oldServiceName)
                .build()
                .toProduct();

        databaseHelper.addProduct(product1);

        Product product2 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withServiceName(oldServiceName)
                .build()
                .toProduct();

        databaseHelper.addProduct(product2);

        Product product3 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(anotherGatewayAccountId)
                .withServiceName(oldServiceName)
                .build()
                .toProduct();

        databaseHelper.addProduct(product3);

        Integer updatedRows = productDao.updateGatewayAccount(gatewayAccountId, newServiceName);
        assertThat(updatedRows, is(2));

        List<ProductEntity> products = productDao.findByGatewayAccountId(gatewayAccountId);
        assertThat(products.size(), is(2));
        assertThat(products.get(0).getServiceName(), is(newServiceName));
        assertThat(products.get(1).getServiceName(), is(newServiceName));

        products = productDao.findByGatewayAccountId(anotherGatewayAccountId);
        assertThat(products.size(), is(1));
        assertThat(products.get(0).getServiceName(), is(oldServiceName));
    }

    @Test
    public void findByFriendlyUrl_shouldReturnAProduct_whenExists() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();
        String friendlyUrl = URLEncoder.encode("kent-council/pay-for-your-fishing-licence", "UTF-8");

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withFriendlyUrl(friendlyUrl)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Optional<ProductEntity> productEntity = productDao.findByFriendlyUrlOrExternalId(friendlyUrl);
        assertTrue(productEntity.isPresent());
        assertThat(productEntity.get().toProduct(), ProductMatcher.isSame(product));
    }

    @Test
    public void findByFriendlyUrl_shouldNotReturnAProduct_whenDoesNotExist() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();
        String friendlyUrl = URLEncoder.encode("kent-council/pay-for-your-parking-permit", "UTF-8");
        String anotherFriendlyUrl = URLEncoder.encode("kent-council-pay-for-your-parking-permit", "UTF-8");

        Product product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withFriendlyUrl(friendlyUrl)
                .build()
                .toProduct();

        databaseHelper.addProduct(product);

        Optional<ProductEntity> productEntity = productDao.findByFriendlyUrlOrExternalId(anotherFriendlyUrl);
        assertFalse(productEntity.isPresent());
    }
}
