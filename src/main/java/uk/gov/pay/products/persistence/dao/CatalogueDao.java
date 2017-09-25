package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;

import javax.persistence.EntityManager;
import java.util.Optional;

public class CatalogueDao extends JpaDao<CatalogueEntity> {

    @Inject
    protected CatalogueDao(Provider<EntityManager> entityManager) {
        super(entityManager, CatalogueEntity.class);
    }

    public Optional<CatalogueEntity> findByExternalId(String externalId) {
        String query = "SELECT catalogue FROM CatalogueEntity catalogue " +
                "WHERE catalogue.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, CatalogueEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();

    }
}
