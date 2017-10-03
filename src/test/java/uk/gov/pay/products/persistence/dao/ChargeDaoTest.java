package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.ChargeEntityFixture;
import uk.gov.pay.products.fixtures.ProductEntityFixture;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ChargeEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.fixtures.CatalogueEntityFixture.aCatalogueEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ChargeDaoTest extends DaoTestBase {

    private ChargeDao chargeDao;
    private ProductDao productDao;

    @Before
    public void before() {
        chargeDao = env.getInstance(ChargeDao.class);
        productDao = env.getInstance(ProductDao.class);
    }

    @Test
    public void shouldSuccess_whenSavingAValidCharge() throws Exception {
        String externalId = randomUuid();
        Long price = 1050L;

        ProductEntity productEntity = createProductGraph();

        ChargeEntity aChargeEntity = ChargeEntityFixture.aChargeEntity()
                .withExternalId(externalId)
                .withPrice(price)
                .withProductExternalId(productEntity.getExternalId())
                .build();

        chargeDao.persist(aChargeEntity);
        Optional<ChargeEntity> expectedChargeEntity = chargeDao.findByExternalId(externalId);
        assertTrue(expectedChargeEntity.isPresent());
        assertThat(expectedChargeEntity.get().getPrice(), is(price));
    }

    private ProductEntity createProductGraph() {
        CatalogueEntity aCatalogueEntity = aCatalogueEntity().build();

        ProductEntity product = ProductEntityFixture.aProductEntity()
                .withCatalogue(aCatalogueEntity)
                .build();

        productDao.persist(product);

        return product;
    }
}