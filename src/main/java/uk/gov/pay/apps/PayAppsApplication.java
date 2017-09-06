package uk.gov.pay.apps;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.apps.config.PayAppsConfiguration;
import uk.gov.pay.apps.healthchecks.Ping;

public class PayAppsApplication extends Application<PayAppsConfiguration> {
    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;

    @Override
    public String getName() {
        return "PayApps";
    }

    @Override
    public void initialize(final Bootstrap<PayAppsConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(NON_STRICT_VARIABLE_SUBSTITUTOR)
                )
        );
    }

    @Override
    public void run(final PayAppsConfiguration configuration,
                    final Environment environment) {
        environment.healthChecks().register("ping", new Ping());
    }

    public static void main(final String[] args) throws Exception {
        new PayAppsApplication().run(args);
    }
}
