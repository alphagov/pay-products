package uk.gov.pay.products.client.publicauth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewApiTokenFromPublicAuthResponse {
    @JsonProperty
    private String token;

    public NewApiTokenFromPublicAuthResponse() {}

    public NewApiTokenFromPublicAuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
