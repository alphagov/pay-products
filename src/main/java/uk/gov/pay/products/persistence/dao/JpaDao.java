package uk.gov.pay.products.persistence.dao;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

import javax.persistence.EntityManager;

@Transactional
public abstract class JpaDao<T> {

    final Provider<EntityManager> entityManager;

    JpaDao(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    public void persist(final T object) {
        entityManager.get().persist(object);
    }

    public void remove(T object) {
        if (!entityManager.get().contains(object)) {
            object = entityManager.get().merge(object);
        }
        entityManager.get().remove(object);
    }

    public T merge(final T object) {
        return entityManager.get().merge(object);
    }
}
