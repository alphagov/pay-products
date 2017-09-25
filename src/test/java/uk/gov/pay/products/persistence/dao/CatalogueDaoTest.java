package uk.gov.pay.products.persistence.dao;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class CatalogueDaoTest extends DaoTestBase {

    CatalogueDao catalogueDao;

    @Before
    public void before() throws Exception {
        catalogueDao = env.getInstance(CatalogueDao.class);
    }

    @Test
    public void shouldSuccess_whenSavingAValidCatalogue() throws Exception {
        CatalogueEntity catalogueEntity = new CatalogueEntity();
        String externalId = randomUuid();
        catalogueEntity.setExternalId(externalId);
        catalogueEntity.setExternalServiceId(randomUuid());
        catalogueEntity.setName("test catalogue");
        catalogueEntity.setStatus("active");
        catalogueDao.persist(catalogueEntity);

        assertThat(catalogueEntity.getVersion(), greaterThan(0L));

        Optional<CatalogueEntity> catalogueEntityOptional = catalogueDao.findByExternalId(externalId);
        assertTrue(catalogueEntityOptional.isPresent());
        assertThat(catalogueEntityOptional.get().getName(), is("test catalogue"));
        assertThat(catalogueEntityOptional.get().getStatus(), is("active"));
    }
}
