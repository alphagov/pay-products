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
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.pay.products.config.PersistenceServiceInitialiser;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.config.ProductsModule;
import uk.gov.pay.products.exception.mapper.BadPaymentRequestExceptionMapper;
import uk.gov.pay.products.exception.mapper.FailToReplaceApiTokenExceptionMapper;
import uk.gov.pay.products.exception.mapper.MetadataNotFoundExceptionMapper;
import uk.gov.pay.products.exception.mapper.FailToGetNewApiTokenExceptionMapper;
import uk.gov.pay.products.exception.mapper.PaymentCreationExceptionMapper;
import uk.gov.pay.products.exception.mapper.PaymentCreatorNotFoundExceptionMapper;
import uk.gov.pay.products.exception.mapper.ProductNotFoundExceptionMapper;
import uk.gov.pay.products.filters.LoggingMDCRequestFilter;
import uk.gov.pay.products.filters.LoggingMDCResponseFilter;
import uk.gov.pay.products.healthchecks.DependentResourceWaitCommand;
import uk.gov.pay.products.healthchecks.Ping;
import uk.gov.pay.products.resources.HealthCheckResource;
import uk.gov.pay.products.resources.PaymentResource;
import uk.gov.pay.products.resources.ProductResource;
import uk.gov.service.payments.commons.utils.healthchecks.DatabaseHealthCheck;
import uk.gov.service.payments.commons.utils.metrics.DatabaseMetricsService;
import uk.gov.service.payments.logging.GovUkPayDropwizardRequestJsonLogLayoutFactory;
import uk.gov.service.payments.logging.LoggingFilter;
import uk.gov.service.payments.logging.LogstashConsoleAppenderFactory;

import java.util.concurrent.TimeUnit;

import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

public class ProductsApplication extends Application<ProductsConfiguration> {
    private static final boolean NON_STRICT_VARIABLE_SUBSTITUTOR = false;
    private static final String SERVICE_METRICS_NODE = "pay-products";
    private static final int GRAPHITE_SENDING_PERIOD_SECONDS = 10;
    private static final String API_VERSION_PATH = "/v1";

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

        bootstrap.addCommand(new DependentResourceWaitCommand());
        bootstrap.getObjectMapper().getSubtypeResolver().registerSubtypes(LogstashConsoleAppenderFactory.class);
        bootstrap.getObjectMapper().getSubtypeResolver().registerSubtypes(GovUkPayDropwizardRequestJsonLogLayoutFactory.class);
    }

    @Override
    public void run(final ProductsConfiguration configuration,
                    final Environment environment) {
        final Injector injector = Guice.createInjector(new ProductsModule(configuration, environment));
        injector.getInstance(PersistenceServiceInitialiser.class);

        initialiseMetrics(configuration, environment);
        environment.servlets().addFilter("LoggingFilter", new LoggingFilter())
                .addMappingForUrlPatterns(of(REQUEST), true, API_VERSION_PATH + "/*");
        environment.jersey().register(injector.getInstance(LoggingMDCRequestFilter.class));
        environment.jersey().register(injector.getInstance(LoggingMDCResponseFilter.class));
        environment.healthChecks().register("ping", new Ping());
        environment.healthChecks().register("database", new DatabaseHealthCheck(configuration.getDataSourceFactory()));
        environment.jersey().register(injector.getInstance(HealthCheckResource.class));
        environment.jersey().register(injector.getInstance(ProductResource.class));
        environment.jersey().register(injector.getInstance(PaymentResource.class));

        attachExceptionMappersTo(environment.jersey());
    }

    private void initialiseMetrics(ProductsConfiguration configuration, Environment environment) {
        DatabaseMetricsService metricsService = new DatabaseMetricsService(configuration.getDataSourceFactory(), environment.metrics(), "products");

        environment
                .lifecycle()
                .scheduledExecutorService("metricscollector")
                .threads(1)
                .build()
                .scheduleAtFixedRate(metricsService::updateMetricData, 0, GRAPHITE_SENDING_PERIOD_SECONDS / 2, TimeUnit.SECONDS);

        GraphiteSender graphiteUDP = new GraphiteUDP(configuration.getGraphiteHost(), configuration.getGraphitePort());
        GraphiteReporter.forRegistry(environment.metrics())
                .prefixedWith(SERVICE_METRICS_NODE)
                .build(graphiteUDP)
                .start(GRAPHITE_SENDING_PERIOD_SECONDS, TimeUnit.SECONDS);

    }

    private void attachExceptionMappersTo(JerseyEnvironment jersey) {
        jersey.register(PaymentCreationExceptionMapper.class);
        jersey.register(PaymentCreatorNotFoundExceptionMapper.class);
        jersey.register(BadPaymentRequestExceptionMapper.class);
        jersey.register(FailToReplaceApiTokenExceptionMapper.class);
        jersey.register(FailToGetNewApiTokenExceptionMapper.class);
        jersey.register(ProductNotFoundExceptionMapper.class);
        jersey.register(MetadataNotFoundExceptionMapper.class);
    }

    public static void main(final String[] args) throws Exception {
        new ProductsApplication().run(args);
    }
}
