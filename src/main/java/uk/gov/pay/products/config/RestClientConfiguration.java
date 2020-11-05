package uk.gov.pay.products.config;

import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;

import javax.validation.constraints.NotNull;

public class RestClientConfiguration extends Configuration {
    private String disabledSecureConnection;

    @NotNull
    private Duration connectTimeout = Duration.seconds(50L);

    @NotNull
    private Duration readTimeout = Duration.seconds(50L);

    public Boolean isDisabledSecureConnection() {
        return "true".equals(disabledSecureConnection);
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }
}
