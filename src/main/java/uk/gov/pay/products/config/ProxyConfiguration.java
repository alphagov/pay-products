package uk.gov.pay.products.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class ProxyConfiguration extends Configuration {

    @NotNull
    private String host;

    @NotNull
    private Integer port;

    @NotNull
    private String enabled;

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public boolean getEnabled() {
        return Boolean.valueOf(enabled);
    }
}
