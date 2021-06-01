package uk.gov.pay.products.client.publicapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.products.client.publicapi.model.Links;
import uk.gov.pay.products.client.publicapi.model.PaymentState;
import uk.gov.pay.products.client.publicapi.model.RefundSummary;
import uk.gov.pay.products.client.publicapi.model.SettlementSummary;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.SupportedLanguageJsonDeserializer;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {
    private String paymentId;
    private String paymentProvider;
    private long amount;
    private PaymentState state;
    private String description;
    private String returnUrl;
    private String reference;
    private String email;
    private String createdDate;
    private RefundSummary refundSummary;
    private SettlementSummary settlementSummary;
    private Links links;
    private SupportedLanguage language;

    public PaymentResponse() {
    }

    public PaymentResponse(
            @JsonProperty("payment_id") String paymentId,
            @JsonProperty("amount") long amount,
            @JsonProperty("state") PaymentState state,
            @JsonProperty("return_url") String returnUrl,
            @JsonProperty("description") String description,
            @JsonProperty("reference") String reference,
            @JsonProperty("email") String email,
            @JsonProperty("payment_provider") String paymentProvider,
            @JsonProperty("created_date") String createdDate,
            @JsonProperty("refund_summary") RefundSummary refundSummary,
            @JsonProperty("settlement_summary") SettlementSummary settlementSummary,
            @JsonProperty("_links") Links links,
            @JsonProperty("language") @JsonDeserialize(using = SupportedLanguageJsonDeserializer.class) SupportedLanguage language) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.email = email;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.links = links;
        this.language = language;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    public void setRefundSummary(RefundSummary refundSummary) {
        this.refundSummary = refundSummary;
    }

    public SettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    public void setSettlementSummary(SettlementSummary settlementSummary) {
        this.settlementSummary = settlementSummary;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", state=" + state +
                ", description='" + description + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", refundSummary=" + refundSummary +
                ", settlementSummary=" + settlementSummary +
                ", links=" + links +
                ", language='" + language.toString() + '\'' +
                '}';
    }
}
