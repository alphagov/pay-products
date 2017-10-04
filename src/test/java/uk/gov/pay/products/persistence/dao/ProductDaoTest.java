package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.fixtures.CatalogueEntityFixture.aCatalogueEntity;
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
}
