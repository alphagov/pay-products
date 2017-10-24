package uk.gov.pay.products.service.transaction;

/**
 * Represents a block of code that should be executed in a transaction boundary
 * In addition to being transactional, this expects to report back database CONFLICT scenarios
 *
 * @param <TransactionContext>
 * @param <R> return value
 */
@FunctionalInterface
public interface PreTransactionalOperation<TransactionContext, R> extends ManagedOperation<TransactionContext, R> {

}
