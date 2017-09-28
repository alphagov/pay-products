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
    private String frontendUrl;

    public String getVcapServices() {
        String vcapServices = System.getenv("VCAP_SERVICES");
        return vcapServices;
    }

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

    public String getFrontendUrl() {
        return frontendUrl;
    }
}
