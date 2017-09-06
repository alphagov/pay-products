package uk.gov.pay.apps;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.apps.config.PayAppsConfiguration;
import uk.gov.pay.apps.healthchecks.Ping;
import uk.gov.pay.apps.util.TrustingSSLSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.TimeUnit;

public class PayAppsApplication extends Application<PayAppsConfiguration> {
    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;
    private static final String SERVICE_METRICS_NODE = "pay-apps";
    private static final int GRAPHITE_SENDING_PERIOD_SECONDS = 10;

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
        initialiseMetrics(configuration, environment);

        setGlobalProxies();
    }

    private void setGlobalProxies() {
        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
    }

    private void initialiseMetrics(PayAppsConfiguration configuration, Environment environment) {
        GraphiteSender graphiteUDP = new GraphiteUDP(configuration.getGraphiteHost(), Integer.valueOf(configuration.getGraphitePort()));
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(SERVICE_METRICS_NODE)
                .build(graphiteUDP)
                .start(GRAPHITE_SENDING_PERIOD_SECONDS, TimeUnit.SECONDS);

    }

    public static void main(final String[] args) throws Exception {
        new PayAppsApplication().run(args);
    }
}
