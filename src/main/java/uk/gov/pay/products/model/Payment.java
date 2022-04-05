package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.products.util.PaymentStatus;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Payment {

    private static final String FIELD_EXTERNAL_ID = "external_id";
    private static final String FIELD_GOVUK_PAYMENT_ID = "govuk_payment_id";
    private static final String FIELD_PRODUCT_EXTERNAL_ID = "product_external_id";
    private static final String FIELD_LINKS = "_links";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FIELD_GOVUK_STATUS = "govuk_status";
    private static final String FIELD_REFERENCE_NUMBER = "reference_number";

    @Schema(example = "h6347634cwb67wii7b6ciueroytw", required = true)
    private final String externalId;
    @Schema(example = "7cs487heites2nne5k17j5j9as")
    private final String govukPaymentId;
    @JsonIgnore
    private final String nextUrl;
    @Schema(example = "uier837y735n837475y3847534", required = true)
    private final String productExternalId;
    private List<Link> links = new ArrayList<>();
    @JsonIgnore
    private final Integer productId;
    @Schema(example = "1050", required = true)
    private Long amount;
    @JsonProperty(FIELD_GOVUK_STATUS)
    @Schema(hidden = true)
    private String govUkStatus;
    @Schema(example = "RE4R2A6VAP")
    private final String referenceNumber;
    @JsonIgnore
    private ZonedDateTime dateCreated;
    @Schema(example = "SUBMITTED", required = true)
    private PaymentStatus status;

    public Payment(
            @JsonProperty(FIELD_EXTERNAL_ID) String externalId,
            @JsonProperty(FIELD_GOVUK_PAYMENT_ID) String govukPaymentId,
            String nextUrl,
            @JsonProperty(FIELD_AMOUNT) Long amount,
            @JsonProperty(FIELD_PRODUCT_EXTERNAL_ID) String productExternalId,
            @JsonProperty(FIELD_STATUS) PaymentStatus status,
            Integer productId,
            @JsonProperty(FIELD_REFERENCE_NUMBER) String referenceNumber) {
        this.externalId = externalId;
        this.govukPaymentId = govukPaymentId;
        this.nextUrl = nextUrl;
        this.productExternalId = productExternalId;
        this.status = status;
        this.productId = productId;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
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

    public Integer getProductId() {
        return this.productId;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    @JsonProperty(FIELD_LINKS)
    @Schema(example = "[" +
            "            {" +
            "                \"rel\": \"self\"," +
            "                \"method\": \"GET\"," +
            "                \"href\": \"https://govukpay-products.cloudapps.digital/v1/api/payments/h6347634cwb67wii7b6ciueroytw\"" +
            "            }," +
            "            {" +
            "                \"rel\": \"next\"," +
            "                \"method\": \"GET\"," +
            "                \"href\": \"https://some.valid.url/paid\"" +
            "            } " +
            "        ]", required = true)
    public List<Link> getLinks() {
        return links;
    }

    public PaymentStatus getStatus() { return status; }

    public void setStatus(PaymentStatus status) { this.status = status; }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getGovUkStatus(){
        return govUkStatus;
    }

    @Schema(example = "success", name = "govuk_status")
    public void setGovukStatus(String status) {
        this.govUkStatus = status;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
}
