package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.fixtures.CatalogueEntityFixture;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.fixtures.ProductEntityFixture.aProductEntity;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class CatalogueDaoTest extends DaoTestBase {

    private CatalogueDao catalogueDao;

    @Before
    public void before() throws Exception {
        catalogueDao = env.getInstance(CatalogueDao.class);
    }

    @Test
    public void shouldSuccess_whenSavingAValidCatalogue() throws Exception {
        String externalId = randomUuid();
        CatalogueEntity catalogueEntity = CatalogueEntityFixture.aCatalogueEntity()
                .withExternalId(externalId)
                .withName("test catalogue")
                .withStatus("active")
                .build();

        catalogueDao.persist(catalogueEntity);

        assertThat(catalogueEntity.getVersion(), greaterThan(0L));

        Optional<CatalogueEntity> catalogueEntityOptional = catalogueDao.findByExternalId(externalId);
        assertTrue(catalogueEntityOptional.isPresent());
        assertThat(catalogueEntityOptional.get().getName(), is(catalogueEntity.getName()));
        assertThat(catalogueEntityOptional.get().getStatus(), is(catalogueEntity.getStatus()));
    }

    @Test
    public void shouldGet_listOfProductsAssociatedToACatalogue() throws Exception {
        CatalogueEntity catalogueEntity = CatalogueEntityFixture.aCatalogueEntity()
                .withExternalId(randomUuid())
                .build();

        catalogueEntity.getProducts().add(aProductEntity().withCatalogue(catalogueEntity).build());
        catalogueEntity.getProducts().add(aProductEntity().withCatalogue(catalogueEntity).build());

        catalogueDao.persist(catalogueEntity);
        Optional<CatalogueEntity> persistedCatalogue = catalogueDao.findByExternalId(catalogueEntity.getExternalId());
        assertTrue(persistedCatalogue.isPresent());
        assertThat(persistedCatalogue.get().getProducts().size(), is(2));
    }
}
