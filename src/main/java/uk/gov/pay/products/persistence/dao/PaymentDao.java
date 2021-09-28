package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

public class PaymentDao extends JpaDao<PaymentEntity> {

    @Inject
    PaymentDao(Provider<EntityManager> entityManager) {
        super(entityManager);
    }

    public Optional<PaymentEntity> findByExternalId(String externalId) {
        String query = "SELECT payment FROM PaymentEntity payment " +
                "WHERE payment.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public List<PaymentEntity> findByProductExternalId(String productExternalId){
        String query = "SELECT payment FROM PaymentEntity payment WHERE payment.product.externalId=:productExternalId";

        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("productExternalId", productExternalId)
                .getResultList();
    }
    
    public Optional<PaymentEntity> findByGatewayAccountIdAndReferenceNumber(Integer gatewayAccountId, String referenceNumber) {
        String query = "SELECT payment FROM PaymentEntity payment " +
                "WHERE payment.gatewayAccountId = :gatewayAccountId " +
                "AND payment.referenceNumber =:referenceNumber";
        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("gatewayAccountId", gatewayAccountId)
                .setParameter("referenceNumber", referenceNumber)
                .getResultList().stream().findFirst();
    }
}
