package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.util.List;
import java.util.Optional;

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
    public void shouldSuccess_whenSavingAValidProduct() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .withName("test name")
                .build();

        productDao.persist(product);

        Optional<ProductEntity> expectedProduct = productDao.findByExternalId(externalId);
        assertTrue(expectedProduct.isPresent());

        assertThat(expectedProduct.get().getName(), is(product.getName()));
    }

    @Test
    public void shouldReturnProductsWithStatusActive() throws Exception {
        String externalId = randomUuid();
        Integer gatewayAccountId = randomInt();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withGatewayAccountId(gatewayAccountId)
                .build();

        productDao.persist(product);

        ProductEntity product_2 = ProductEntityFixture.aProductEntity()
                .withGatewayAccountId(gatewayAccountId)
                .withStatus(ProductStatus.INACTIVE)
                .build();

        productDao.persist(product_2);

        List<ProductEntity> products = productDao.findByGatewayAccountId(gatewayAccountId);
        assertThat(products.size(), is(1));
        assertThat(products.get(0).getExternalId(), is(externalId));
        assertThat(products.get(0).getStatus(), is(ProductStatus.ACTIVE));
    }
}
