package uk.gov.pay.products.service.transaction;

/**
 * Represents an executable block of code which need to be performed as part of a Transaction Flow.
 * This could either be a Transactional or NonTransactional.
 * @param <T>
 * @param <R>
 *
 *     @see TransactionalOperation
 *     @see NonTransactionalOperation
 */
@FunctionalInterface
public interface ManagedOperation<T, R> {

    /**
     * executes this managed operation
     * @param input of this execution
     * @return R result of execution
     */
    R execute(T input);
}
