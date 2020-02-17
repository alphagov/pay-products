package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class ProductMetadataDao extends JpaDao<ProductMetadataEntity> {

    @Inject
    public ProductMetadataDao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public List<ProductMetadataEntity> findByProductsId(Integer productId) {
        String query = "SELECT metadata FROM ProductMetadataEntity metadata " +
                "WHERE metadata.id = :productId";

        return entityManager.get()
                .createQuery(query, ProductMetadataEntity.class)
                .setParameter("productId", productId)
                .getResultList();
    }
}
