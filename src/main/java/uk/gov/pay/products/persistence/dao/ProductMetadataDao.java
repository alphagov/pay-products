package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import javax.persistence.EntityManager;

public class ProductMetadataDao extends JpaDao<ProductMetadataEntity> {

    @Inject
    ProductMetadataDao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }
}
