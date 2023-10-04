package uk.gov.pay.products.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Optional;

public class ProductsConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("expungeHistoricalDataConfig")
    private ExpungeHistoricalDataConfig expungeHistoricalDataConfig;
    
    @Valid
    @NotNull
    private DataSourceFactory dataSourceFactory;

    @Valid
    @NotNull
    private JPAConfiguration jpaConfiguration;

    @NotNull
    private String baseUrl;

    @NotNull
    private String publicApiUrl;

    @NotNull
    private String publicAuthUrl;

    @NotNull
    private String productsUiPayUrl;

    @NotNull
    private String productsUiConfirmUrl;

    @NotNull
    private String friendlyBaseUri;

    @NotNull
    private boolean returnUrlMustBeSecure = true;

    @NotNull
    private String emailAddressForReplacingApiTokens;

    public String getVcapServices() {
        return System.getenv("VCAP_SERVICES");
    }

    @Valid
    @NotNull
    @JsonProperty("jerseyClientConfiguration")
    private RestClientConfiguration restClientConfiguration;

    @JsonProperty("ecsContainerMetadataUriV4")
    private URI ecsContainerMetadataUriV4;

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    @JsonProperty("jpa")
    public JPAConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPublicApiUrl() {
        return publicApiUrl;
    }

    public String getPublicAuthUrl() {
        return publicAuthUrl;
    }

    public String getProductsUiPayUrl() {
        return productsUiPayUrl;
    }

    public RestClientConfiguration getRestClientConfiguration() {
        return restClientConfiguration;
    }

    public String getProductsUiConfirmUrl() { return productsUiConfirmUrl;
    }

    public String getFriendlyBaseUri() { return friendlyBaseUri; }

    public boolean getReturnUrlMustBeSecure() {
        return returnUrlMustBeSecure;
    }

    public String getEmailAddressForReplacingApiTokens() {
        return emailAddressForReplacingApiTokens;
    }

    public Optional<URI> getEcsContainerMetadataUriV4() {
        return Optional.ofNullable(ecsContainerMetadataUriV4);
    }

    public ExpungeHistoricalDataConfig getExpungeHistoricalDataConfig() {
        return expungeHistoricalDataConfig;
    }
}
