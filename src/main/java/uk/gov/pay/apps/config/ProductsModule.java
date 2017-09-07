package uk.gov.pay.apps.config;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

public class ProductsModule extends AbstractModule {

    final PayAppsConfiguration configuration;
    final Environment environment;

    public ProductsModule(PayAppsConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        bind(DataSourceFactory.class).toInstance(configuration.getDataSourceFactory());
        bind(MetricRegistry.class).toInstance(environment.metrics());
        bind(Environment.class).toInstance(environment);
    }
}
