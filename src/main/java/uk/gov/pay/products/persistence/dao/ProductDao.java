package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import javax.persistence.EntityManager;
import java.util.List;
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

    public List<ProductEntity> findByGatewayAccountId(Integer gatewayAccountId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.gatewayAccountId = :gatewayAccountId " +
                "AND product.status = :status";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("gatewayAccountId", gatewayAccountId)
                .setParameter("status", ProductStatus.ACTIVE)
                .getResultList();
    }

    public Integer updateGatewayAccount(Integer gatewayAccountId, String serviceName) {
        String query = "UPDATE ProductEntity p SET p.serviceName = :serviceName WHERE p.gatewayAccountId = :gatewayAccountId";

        return entityManager.get().createQuery(query)
                .setParameter("serviceName", serviceName)
                .setParameter("gatewayAccountId", gatewayAccountId)
                .executeUpdate();
    }
}
