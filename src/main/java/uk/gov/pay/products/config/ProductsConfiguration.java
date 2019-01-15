package uk.gov.pay.products.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ProductsConfiguration extends Configuration {

    @NotNull
    private String graphiteHost;
    @NotNull
    private String graphitePort;

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
    private String productsUiPayUrl;

    @NotNull
    private String productsUiConfirmUrl;

    @NotNull
    private String friendlyBaseUri;

    public String getVcapServices() {
        return System.getenv("VCAP_SERVICES");
    }

    @Valid
    @NotNull
    @JsonProperty("jerseyClientConfiguration")
    private RestClientConfiguration restClientConfiguration;

    public String getGraphiteHost() {
        return graphiteHost;
    }

    public String getGraphitePort() {
        return graphitePort;
    }

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

    public String getProductsUiPayUrl() {
        return productsUiPayUrl;
    }

    public RestClientConfiguration getRestClientConfiguration() {
        return restClientConfiguration;
    }

    public String getProductsUiConfirmUrl() { return productsUiConfirmUrl;
    }

    public String getFriendlyBaseUri() { return friendlyBaseUri; }
}
