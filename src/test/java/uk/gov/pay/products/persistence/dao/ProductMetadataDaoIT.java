package uk.gov.pay.products.persistence.dao;

import org.apache.commons.lang3.RandomUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.fixtures.ProductMetadataEntityFixture;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import static org.hamcrest.Matchers.is;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductMetadataDaoIT extends DaoTestBase {
    private ProductMetadataDao productMetadataDao;  
    private ProductMetadataEntity productMetadataEntity;
    private ProductDao productDao;
    private ProductEntity productEntity;

    String productExternalId = randomUuid();
    private Integer id = RandomUtils.nextInt();

    @Before
    public void before() {
        productDao = env.getInstance(ProductDao.class);
        productMetadataDao = env.getInstance(ProductMetadataDao.class);
        productEntity = ProductEntityFixture.aProductEntity()
                .withExternalId(productExternalId)
                .build();
        productDao.persist(productEntity);
        productMetadataEntity = ProductMetadataEntityFixture.aProductMetadataEntity()
                .withProductEntity(productEntity)
                .withMetadataKey("a key")
                .withMetadataValue("a value")
                .build();
        productMetadataDao.persist(productMetadataEntity);
        id = productMetadataEntity.getId();
    }

    @Test
    public void productMetadataDaoShouldReturnEntity_whenIdExists() {
        ProductMetadataEntity productMetadataEntityReturned = productMetadataDao.entityManager
                .get()
                .find(ProductMetadataEntity.class, id);
        MatcherAssert.assertThat(productMetadataEntityReturned.getMetadataKey(), is("a key"));
        MatcherAssert.assertThat(productMetadataEntityReturned.getMetadataValue(), is("a value"));
        MatcherAssert.assertThat(productMetadataEntityReturned.getId(), is(id));
        MatcherAssert.assertThat(productMetadataEntityReturned.getProductId().getExternalId(), is(productExternalId));
    }
}
