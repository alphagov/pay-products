package uk.gov.pay.products.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.products.util.PaymentStatus;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Payment {

    private static final String FIELD_EXTERNAL_ID = "external_id";
    private static final String FIELD_GOVUK_PAYMENT_ID = "govuk_payment_id";
    private static final String FIELD_NEXT_URL = "next_url";
    private static final String FIELD_PRODUCT_EXTERNAL_ID = "product_external_id";
    private static final String FIELD_LINKS = "_links";
    private static final String FIELD_STATUS = "status";

    private String externalId;
    private String govukPaymentId;
    private String nextUrl;
    private String productExternalId;
    private List<Link> links = new ArrayList<>();
    @JsonIgnore
    private Integer productId;

    private PaymentStatus status;

    public Payment(
            @JsonProperty(FIELD_EXTERNAL_ID) String externalId,
            @JsonProperty(FIELD_GOVUK_PAYMENT_ID) String govukPaymentId,
            @JsonProperty(FIELD_NEXT_URL) String nextUrl,
            @JsonProperty(FIELD_PRODUCT_EXTERNAL_ID) String productExternalId,
            @JsonProperty(FIELD_STATUS) PaymentStatus status,
            Integer productId) {
        this.externalId = externalId;
        this.govukPaymentId = govukPaymentId;
        this.nextUrl = nextUrl;
        this.productExternalId = productExternalId;
        this.status = status;
        this.productId = productId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getGovukPaymentId() {
        return govukPaymentId;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getProductExternalId() {
        return productExternalId;
    }

    public Integer getProductId(){
        return this.productId;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @JsonProperty(FIELD_LINKS)
    public List<Link> getLinks() {
        return links;
    }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }
}
