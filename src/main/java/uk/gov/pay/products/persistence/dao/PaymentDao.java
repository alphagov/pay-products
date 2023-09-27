package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
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

    /*
    The govukPaymentId is equivalent to ledger's resource_external_id
     */
    public Optional<PaymentEntity> findByGovukPaymentId(String govukPaymentId) {
        String query = "SELECT payment FROM PaymentEntity payment " +
                "WHERE payment.govukPaymentId = :govukPaymentId";

        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("govukPaymentId", govukPaymentId)
                .getResultList().stream().findFirst();
    }

    public List<PaymentEntity> getPaymentsForDeletion(ZonedDateTime maxDate, int maxNumberOfPayments) {
        String query = "SELECT payment FROM PaymentEntity payment WHERE payment.dateCreated < :maxDate " +
                "ORDER BY payment.dateCreated";
        
        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("maxDate", maxDate)
                .setMaxResults(maxNumberOfPayments)
                .getResultList();
    }
}
