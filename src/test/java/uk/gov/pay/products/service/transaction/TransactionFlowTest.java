package uk.gov.pay.products.service.transaction;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TransactionFlowTest {

    @Test
    public void shouldExecuteANonTransactionalOperationAndPreserveResultInContext() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((NonTransactionalOperation<TransactionContext, String>) ctx -> "Foo");

        verify(mockContext, times(1)).put("Foo");
        assertThat(flow.complete(), is(mockContext));
    }

    @Test
    public void shouldExecuteATransactionalOperationAndPreserveResultInContext() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((TransactionalOperation<TransactionContext, String>) ctx -> "Bar");
        verify(mockContext, times(1)).put("Bar");

        assertThat(flow.complete(), is(mockContext));
    }

    @Test(expected = NullPointerException.class)
    public void shouldErrorIfProvidedOperationIsNull_forTransactionalOperation() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((TransactionalOperation<TransactionContext, String>) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldErrorIfProvidedOperationIsNull_forNonTransactionalOperation() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((NonTransactionalOperation<TransactionContext, String>) null);
    }
}
