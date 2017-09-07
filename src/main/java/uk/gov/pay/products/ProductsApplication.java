package uk.gov.pay.products;

import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.config.ProductsModule;
import uk.gov.pay.products.healthchecks.DatabaseHealthCheck;
import uk.gov.pay.products.healthchecks.Ping;
import uk.gov.pay.products.resources.HealthCheckResource;
import uk.gov.pay.products.util.TrustingSSLSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.util.concurrent.TimeUnit;

public class ProductsApplication extends Application<ProductsConfiguration> {
    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;
    private static final String SERVICE_METRICS_NODE = "pay-products";
    private static final int GRAPHITE_SENDING_PERIOD_SECONDS = 10;

    @Override
    public String getName() {
        return "Products";
    }

    @Override
    public void initialize(final Bootstrap<ProductsConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(NON_STRICT_VARIABLE_SUBSTITUTOR)
                )
        );

        bootstrap.addBundle(new MigrationsBundle<ProductsConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ProductsConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(final ProductsConfiguration configuration,
                    final Environment environment) {
        final Injector injector = Guice.createInjector(new ProductsModule(configuration, environment));

        initialiseMetrics(configuration, environment);
        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", injector.getInstance(DatabaseHealthCheck.class));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));

        setGlobalProxies();
    }

    private void setGlobalProxies() {
        SSLSocketFactory socketFactory = new TrustingSSLSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
    }

    private void initialiseMetrics(ProductsConfiguration configuration, Environment environment) {
        GraphiteSender graphiteUDP = new GraphiteUDP(configuration.getGraphiteHost(), Integer.valueOf(configuration.getGraphitePort()));
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(SERVICE_METRICS_NODE)
                .build(graphiteUDP)
                .start(GRAPHITE_SENDING_PERIOD_SECONDS, TimeUnit.SECONDS);

    }

    public static void main(final String[] args) throws Exception {
        new ProductsApplication().run(args);
    }
}
