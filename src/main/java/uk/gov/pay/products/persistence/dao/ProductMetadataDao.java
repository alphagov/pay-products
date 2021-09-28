package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class ProductMetadataDao extends JpaDao<ProductMetadataEntity> {

    @Inject
    public ProductMetadataDao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<ProductMetadataEntity> findByProductsExternalId(String productExternalId) {
        String query = "SELECT metadata FROM ProductMetadataEntity metadata " +
                "WHERE metadata.productEntity.externalId = :productExternalId ";

        return entityManager.get()
                .createQuery(query, ProductMetadataEntity.class)
                .setParameter("productExternalId", productExternalId)
                .getResultList();
    }

    public Optional<ProductMetadataEntity> findByProductsExternalIdAndKey(String productExternalId, String key) {
        String query = "SELECT metadata FROM ProductMetadataEntity metadata " +
                "WHERE metadata.productEntity.externalId = :productExternalId " +
                "AND lower(metadata.metadataKey) = :key";
        return entityManager.get()
                .createQuery(query, ProductMetadataEntity.class)
                .setParameter("productExternalId", productExternalId)
                .setParameter("key", key.toLowerCase())
                .getResultList().stream().findFirst();
    }

    public void deleteForProductExternalId(String productExternalId) {
        String query = "delete FROM ProductMetadataEntity metadata " +
                "WHERE metadata.productEntity.externalId = :productExternalId ";

        entityManager.get()
                .createQuery(query, ProductMetadataEntity.class)
                .setParameter("productExternalId", productExternalId)
                .executeUpdate();
    }
}
