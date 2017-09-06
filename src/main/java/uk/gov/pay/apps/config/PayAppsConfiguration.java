package uk.gov.pay.apps.config;

import io.dropwizard.Configuration;

public class PayAppsConfiguration extends Configuration {

    public String getVcapServices() {
        String vcapServices = System.getenv("VCAP_SERVICES");
        return vcapServices;
    }
}
