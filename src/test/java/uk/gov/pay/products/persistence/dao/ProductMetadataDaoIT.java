package uk.gov.pay.products.persistence.dao;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.fixtures.ProductMetadataEntityFixture;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
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
        assertThat(productMetadataEntityReturned.getMetadataKey(), is("a key"));
        assertThat(productMetadataEntityReturned.getMetadataValue(), is("a value"));
        assertThat(productMetadataEntityReturned.getId(), is(id));
        assertThat(productMetadataEntityReturned.getProductEntity().getExternalId(), is(productExternalId));
    }

    @Test
    public void productMetadataDaoShouldReturnAList_whenProductIdExists() {
        List<ProductMetadataEntity> metadataEntityList = productMetadataDao.findByProductsId(productEntity.getId());
        assertThat(metadataEntityList.size(), is(1));
    }
}
