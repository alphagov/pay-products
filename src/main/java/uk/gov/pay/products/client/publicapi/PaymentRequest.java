package uk.gov.pay.products.client.publicapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import uk.gov.service.payments.commons.model.Source;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PaymentRequest {

    private long amount;
    private String reference;
    private String description;
    private String returnUrl;
    private SupportedLanguage language;
    private boolean moto;
    private Internal internal;
    private Map<String, String> metadata;

    public PaymentRequest(
            @JsonProperty("amount") long amount,
            @JsonProperty("reference") String reference,
            @JsonProperty("description") String description,
            @JsonProperty("return_url") String returnUrl,
            @JsonProperty("language") @JsonSerialize(using = ToStringSerializer.class) SupportedLanguage language,
            @JsonProperty("moto") boolean moto,
            @JsonProperty("metadata") Map<String, String> metadata,
            Source source) {
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.returnUrl = returnUrl;
        this.language = language;
        this.moto = moto;
        this.metadata = metadata;
        this.internal = new Internal(source);
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

    public boolean isMoto() {
        return moto;
    }

    public void setMoto(boolean moto) {
        this.moto = moto;
    }

    public Internal getInternal() {
        return internal;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", reference='" + reference + '\'' +
                ", description='" + description + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", language='" + language.toString() + '\'' +
                ", moto=" + moto +
                ", metadata='" + metadata == null ? "" : metadata.toString() + '\'' +
                ", source='" + internal.getSource() + '\'' +
                '}';
    }
}
