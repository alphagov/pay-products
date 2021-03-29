package uk.gov.pay.products.client.publicauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.service.payments.commons.model.TokenPaymentType;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateApiTokenRequest {
    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("token_type")
    private TokenPaymentType tokenPaymentType;

    @JsonProperty("type")
    private TokenSource tokenSource;

    public CreateApiTokenRequest() {}

    public CreateApiTokenRequest(String accountId,
                                 String description,
                                 String createdBy,
                                 TokenPaymentType tokenPaymentType,
                                 TokenSource tokenSource) {
        this.accountId = accountId;
        this.description = description;
        this.createdBy = createdBy;
        this.tokenPaymentType = tokenPaymentType;
        this.tokenSource = tokenSource;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public TokenPaymentType getTokenPaymentType() {
        return tokenPaymentType;
    }

    public TokenSource getTokenSource() {
        return tokenSource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CreateApiTokenRequest)) {
            return false;
        }

        CreateApiTokenRequest that = (CreateApiTokenRequest) o;

        return accountId.equals(that.accountId) &&
                description.equals(that.description) &&
                createdBy.equals(that.createdBy) &&
                tokenPaymentType == that.tokenPaymentType &&
                tokenSource == that.tokenSource;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, description, createdBy, tokenPaymentType, tokenSource);
    }
}
