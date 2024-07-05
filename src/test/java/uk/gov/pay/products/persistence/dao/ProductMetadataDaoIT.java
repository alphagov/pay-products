package uk.gov.pay.products.persistence.dao;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.fixtures.ProductMetadataEntityFixture;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.List;
import java.util.Optional;

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
    public void productMetadataDaoShouldReturnAList_whenProductExternalIdExists() {
        List<ProductMetadataEntity> metadataEntityList =
                productMetadataDao.findByProductsExternalId(productEntity.getExternalId());
        assertThat(metadataEntityList.size(), is(1));
    }

    @Test
    public void productMetadataDaoShouldReturnAMetadataEntity() {
        Optional<ProductMetadataEntity> productMetadata =
                productMetadataDao.findByProductsExternalIdAndKey(productEntity.getExternalId(), productMetadataEntity.getMetadataKey());
        assertThat(productMetadata.isPresent(), is(true));
    }

    @Test
    public void productMetadataDaoShouldUpdateAMetadataEntity() {
        productMetadataEntity.setMetadataValue("new value");
        productMetadataDao.merge(productMetadataEntity);
        Optional<ProductMetadataEntity> productMetadata =
                productMetadataDao.findByProductsExternalIdAndKey(productEntity.getExternalId(), productMetadataEntity.getMetadataKey());
        assertThat(productMetadata.isPresent(), is(true));
        assertThat(productMetadata.get().getMetadataValue(), is("new value"));
    }

    @Test
    public void productMetadataDaoShouldDeleteMetadataForProductExternalId() {
        String externalId = randomUuid();
        ProductEntity entity = ProductEntityFixture.aProductEntity()
                .withExternalId(externalId)
                .build();
        productDao.persist(entity);
        ProductMetadataEntity metadataEntity = ProductMetadataEntityFixture.aProductMetadataEntity()
                .withProductEntity(entity)
                .withMetadataKey("a key")
                .withMetadataValue("a value")
                .build();
        productMetadataDao.persist(metadataEntity);

        List<ProductMetadataEntity> productMetadataList = productMetadataDao.findByProductsExternalId(externalId);
        assertThat(productMetadataList.size(), is(1));

        productMetadataDao.deleteForProductExternalId(externalId);

        productMetadataList = productMetadataDao.findByProductsExternalId(externalId);
        assertThat(productMetadataList.size(), is(0));

        List<ProductMetadataEntity> productMetadata = productMetadataDao.findByProductsExternalId(productExternalId);
        assertThat(productMetadata.size(), is(1));
    }
}
