package uk.gov.pay.apps.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class PayAppsConfiguration extends Configuration {

    @NotNull
    private String graphiteHost;
    @NotNull
    private String graphitePort;

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
}
