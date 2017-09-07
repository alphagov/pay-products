package uk.gov.pay.products.config;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

public class ProductsModule extends AbstractModule {

    final ProductsConfiguration configuration;
    final Environment environment;

    public ProductsModule(ProductsConfiguration configuration, Environment environment) {
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
