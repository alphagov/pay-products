package uk.gov.pay.products.config;

import io.dropwizard.Configuration;

public class RestClientConfiguration extends Configuration {
    private String disabledSecureConnection;

    public Boolean isDisabledSecureConnection() {
        return "true".equals(disabledSecureConnection);
    }

}
