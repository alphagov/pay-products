package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.persistence.entity.PaymentEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentDao extends JpaDao<PaymentEntity> {

    @Inject
    PaymentDao(Provider<EntityManager> entityManager) {
        super(entityManager, PaymentEntity.class);
    }

    public Optional<PaymentEntity> findByExternalId(String externalId) {
        String query = "SELECT payment FROM PaymentEntity payment " +
                "WHERE payment.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public List<PaymentEntity> findByProductId(Integer productId){
        String query = "SELECT payment FROM PaymentEntity payment " +
                "WHERE payment.productEntity.id = :productId";

        return entityManager.get()
                .createQuery(query, PaymentEntity.class)
                .setParameter("productId", productId)
                .getResultList();
    }
}
