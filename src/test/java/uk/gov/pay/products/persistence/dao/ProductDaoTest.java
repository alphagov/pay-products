package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.fixtures.CatalogueEntityFixture.aCatalogueEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductDaoTest extends DaoTestBase {

    private ProductDao productDao;
    private CatalogueDao catalogueDao;

    @Before
    public void before() {
        productDao = env.getInstance(ProductDao.class);
        catalogueDao = env.getInstance(CatalogueDao.class);
    }

    @Test
    public void shouldSuccess_whenSavingAValidProduct() throws Exception {
        String externalId = randomUuid();

        CatalogueEntity aCatalogueEntity = aCatalogueEntity().withExternalId(randomUuid()).build();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withCatalogue(aCatalogueEntity)
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
        String externalServiceId = randomUuid();

        CatalogueEntity aCatalogueEntity = aCatalogueEntity()
                .withExternalId(randomUuid())
                .withExternalServiceId(externalServiceId)
                .build();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .withCatalogue(aCatalogueEntity)
                .build();

        ProductEntity product_2 = ProductEntityFixture.aProductEntity()
                .withCatalogue(aCatalogueEntity)
                .withStatus(ProductStatus.INACTIVE)
                .build();

        aCatalogueEntity.getProducts().add(product);
        aCatalogueEntity.getProducts().add(product_2);
        catalogueDao.persist(aCatalogueEntity);

        List<Product> products = productDao.findByExternalServiceId(externalServiceId);
        assertThat(products.size(), is(1));
        assertThat(products.get(0).getExternalId(), is(externalId));
        assertThat(products.get(0).getStatus(), is(ProductStatus.ACTIVE));
    }
}
