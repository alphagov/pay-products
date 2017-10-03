package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.persistence.entity.ChargeEntity;

import javax.persistence.EntityManager;
import java.util.Optional;

public class ChargeDao extends JpaDao<ChargeEntity> {

    @Inject
    public ChargeDao(Provider<EntityManager> entityManager) {
        super(entityManager, ChargeEntity.class);
    }

    public Optional<ChargeEntity> findByExternalId(String externalId) {
        String query = "SELECT charge from ChargeEntity charge " +
                "WHERE charge.externalId = :externalId";
        return entityManager.get()
                .createQuery(query, ChargeEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }
}
