package uk.gov.pay.apps;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class PayAppsApplication extends Application<PayAppsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new PayAppsApplication().run(args);
    }

    @Override
    public String getName() {
        return "PayApps";
    }

    @Override
    public void initialize(final Bootstrap<PayAppsConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final PayAppsConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
