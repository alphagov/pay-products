package uk.gov.pay.products.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ExpungeHistoricalDataConfig {
    
    @NotNull
    private boolean expungeHistoricalDataEnabled;
    
    @NotNull
    @Min(value = 0L)
    private int expungeDataOlderThanDays;
    
    @NotNull
    @Min(value = 0L)
    private int numberOfPaymentsToExpunge;

    public boolean isExpungeHistoricalDataEnabled() {
        return expungeHistoricalDataEnabled;
    }

    public int getExpungeDataOlderThanDays() {
        return expungeDataOlderThanDays;
    }

    public int getNumberOfPaymentsToExpunge() {
        return numberOfPaymentsToExpunge;
    }
}
