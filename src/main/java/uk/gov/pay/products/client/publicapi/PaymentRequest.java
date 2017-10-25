package uk.gov.pay.products.client.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymentRequest {

    private long amount;
    private String reference;
    private String description;
    private String returnUrl;

    public PaymentRequest(
            @JsonProperty("amount") long amount,
            @JsonProperty("reference") String reference,
            @JsonProperty("description") String description,
            @JsonProperty("return_url") String returnUrl) {
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                '}';
    }
}
