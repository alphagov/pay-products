package uk.gov.pay.products.client.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.pay.commons.model.SupportedLanguage;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PaymentRequest {

    private long amount;
    private String reference;
    private String description;
    private String returnUrl;
    private SupportedLanguage language;

    public PaymentRequest(
            @JsonProperty("amount") long amount,
            @JsonProperty("reference") String reference,
            @JsonProperty("description") String description,
            @JsonProperty("return_url") String returnUrl,
            @JsonProperty("language") @JsonSerialize(using = ToStringSerializer.class) SupportedLanguage language) {
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.language = language;
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

    public SupportedLanguage getLanguage() {
        return language;
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
