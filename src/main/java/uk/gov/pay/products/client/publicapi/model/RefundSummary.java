package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class RefundSummary {

    private String status;
    private long amountAvailable;
    private long amountSubmitted;

    public RefundSummary(
            @JsonProperty("status") String status,
            @JsonProperty("amount_available") long amountAvailable,
            @JsonProperty("amount_submitted") long amountSubmitted) {
        this.status = status;
        this.amountAvailable = amountAvailable;
        this.amountSubmitted = amountSubmitted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(long amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public long getAmountSubmitted() {
        return amountSubmitted;
    }

    public void setAmountSubmitted(long amountSubmitted) {
        this.amountSubmitted = amountSubmitted;
    }

    @Override
    public String toString() {
        return "RefundSummary{" +
                "status='" + status + '\'' +
                ", amountAvailable=" + amountAvailable +
                ", amountSubmitted=" + amountSubmitted +
                '}';
    }
}
