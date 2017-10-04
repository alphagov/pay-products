package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import javax.persistence.EntityManager;
import java.util.Optional;

public class ProductDao extends JpaDao<ProductEntity> {

    @Inject
    protected ProductDao(Provider<EntityManager> entityManager) {
        super(entityManager, ProductEntity.class);
    }

    public Optional<ProductEntity> findByExternalId(String externalId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public void disableByExternalId(String externalId) {
        String query = "UPDATE ProductEntity product " +
                "SET product.status = :newStatus " +
                "WHERE product.externalId = :externalId";

        entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("newStatus", ProductStatus.INACTIVE)
                .setParameter("externalId", externalId)
                .executeUpdate();
    }
}
