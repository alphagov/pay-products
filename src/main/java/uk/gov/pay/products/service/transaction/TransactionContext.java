package uk.gov.pay.products.service.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context class to be used by {@link TransactionFlow}
 * backed by {@link ConcurrentHashMap}
 *
 * Collects intermediate parameter values during a transaction.
 * <b>Only a maximum one object of a given type can exist during a transaction flow.</b>
 */
public class TransactionContext {

    private final Map<Class<?>, Object> params;

    /**
     * only to be constructed by TransactionFlow
     */
    TransactionContext() {
        params = new ConcurrentHashMap<>();
    }

    /**
     * only to be used by TransactionFlow
     * @param param
     * @param <T>
     */
    <T> void put(T param) {
        params.put(param.getClass(), param);
    }

    public <T> T get(Class<T> paramClass) {
        return (T) params.get(paramClass);
    }
}
