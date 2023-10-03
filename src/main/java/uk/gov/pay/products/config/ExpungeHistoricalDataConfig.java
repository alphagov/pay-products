package uk.gov.pay.products.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ExpungeHistoricalDataConfig {
    
    @NotNull
    private boolean expungeHistoricalDataEnabled;
    
    @NotNull
    @Min(value = 0L)
    private int expungeDataOlderThanDays;
    
    @NotNull
    @Min(value = 0L)
    private int numberOfTransactionsToExpunge;

    public boolean isExpungeHistoricalDataEnabled() {
        return expungeHistoricalDataEnabled;
    }

    public int getExpungeDataOlderThanDays() {
        return expungeDataOlderThanDays;
    }

    public int getNumberOfTransactionsToExpunge() {
        return numberOfTransactionsToExpunge;
    }
}
