package uk.gov.pay.products.config;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;

import java.util.Properties;

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

        install(jpaModule(configuration));

    }

    private JpaPersistModule jpaModule(ProductsConfiguration configuration) {
        DataSourceFactory dbConfig = configuration.getDataSourceFactory();
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", dbConfig.getDriverClass());
        properties.put("javax.persistence.jdbc.url", dbConfig.getUrl());
        properties.put("javax.persistence.jdbc.user", dbConfig.getUser());
        properties.put("javax.persistence.jdbc.password", dbConfig.getPassword());

        JPAConfiguration jpaConfiguration = configuration.getJpaConfiguration();
        properties.put("eclipselink.logging.level", jpaConfiguration.getJpaLoggingLevel());
        properties.put("eclipselink.logging.level.sql", jpaConfiguration.getSqlLoggingLevel());
        properties.put("eclipselink.query-results-cache", jpaConfiguration.getCacheSharedDefault());
        properties.put("eclipselink.cache.shared.default", jpaConfiguration.getCacheSharedDefault());
        properties.put("eclipselink.ddl-generation.output-mode", jpaConfiguration.getDdlGenerationOutputMode());
        properties.put("eclipselink.session.customizer", "uk.gov.pay.products.app.config.ProductsSessionCustomiser");

        final JpaPersistModule jpaModule = new JpaPersistModule("ProductsUnit");
        jpaModule.properties(properties);

        return jpaModule;
    }
}
