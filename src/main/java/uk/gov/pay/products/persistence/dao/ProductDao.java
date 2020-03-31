package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.model.ProductUsageStat;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class ProductDao extends JpaDao<ProductEntity> {

    @Inject
    protected ProductDao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public Optional<ProductEntity> findByExternalId(String externalId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> findById(Integer id) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.id = :id";
        
        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("id", id)
                .getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> findByGatewayAccountIdAndExternalId(Integer gatewayAccountId, String externalId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.externalId = :externalId " +
                "AND product.gatewayAccountId = :gatewayAccountId";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("externalId", externalId)
                .setParameter("gatewayAccountId", gatewayAccountId)
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

    public Optional<ProductEntity> findByProductPath(String serviceNamePath, String productNamePath) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.serviceNamePath = :serviceNamePath " +
                "AND product.productNamePath = :productNamePath";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("serviceNamePath", serviceNamePath)
                .setParameter("productNamePath", productNamePath)
                .getResultList().stream().findFirst();
    }

    public List<ProductUsageStat> findProductsAndUsage() {
        return findProductsAndUsageQuery(null);
    }

    public List<ProductUsageStat> findProductsAndUsage(Integer gatewayAccountId) {
        return findProductsAndUsageQuery(gatewayAccountId);
    }

    private List<ProductUsageStat> findProductsAndUsageQuery(Integer gatewayAccountId) {
        Boolean shouldFilterGatewayAccount = gatewayAccountId != null;
        String conditionalFilter = shouldFilterGatewayAccount ?
                "AND payments.product.gatewayAccountId = :gatewayAccountId " :
                "";
        String query = "SELECT new uk.gov.pay.products.model.ProductUsageStat(" +
                "COUNT(1)," +
                "MAX(payments.dateCreated)," +
                "payments.product) " +
                "FROM PaymentEntity payments " +
                "WHERE payments.product.type = :type " +
                conditionalFilter +
                "GROUP BY payments.product";

        TypedQuery<ProductUsageStat> queryBuilder = entityManager.get()
                .createQuery(query, ProductUsageStat.class)
                .setParameter("type", ProductType.ADHOC);

        if (shouldFilterGatewayAccount) {
            queryBuilder.setParameter("gatewayAccountId", gatewayAccountId);
        }
        return queryBuilder.getResultList();
    }
}
