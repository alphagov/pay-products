package uk.gov.pay.products.config;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.setup.Environment;
import uk.gov.pay.products.client.RestClientFactory;
import uk.gov.pay.products.client.publicapi.PublicApiRestClient;
import uk.gov.pay.products.service.LinksDecorator;
import uk.gov.pay.products.service.PaymentFactory;
import uk.gov.pay.products.service.PaymentFinder;
import uk.gov.pay.products.service.ProductApiTokenManager;
import uk.gov.pay.products.service.ProductFactory;
import uk.gov.pay.products.service.ProductFinder;
import uk.gov.pay.products.validations.ProductRequestValidator;
import uk.gov.pay.products.validations.RequestValidations;

import javax.ws.rs.client.Client;
import java.time.Clock;
import java.util.Properties;

public class ProductsModule extends AbstractModule {

    private final ProductsConfiguration configuration;
    private final Environment environment;

    public ProductsModule(ProductsConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    @Override
    protected void configure() {
        final Client client = RestClientFactory.buildClient(configuration.getRestClientConfiguration());

        bind(Client.class).toInstance(client);
        bind(ProductsConfiguration.class).toInstance(configuration);
        bind(DataSourceFactory.class).toInstance(configuration.getDataSourceFactory());
        bind(MetricRegistry.class).toInstance(environment.metrics());
        bind(Environment.class).toInstance(environment);
        bind(RequestValidations.class).in(Singleton.class);
        bind(ProductRequestValidator.class).in(Singleton.class);
        bind(LinksDecorator.class).toInstance(
                new LinksDecorator(
                        configuration.getBaseUrl(),
                        configuration.getProductsUiPayUrl(),
                        configuration.getFriendlyBaseUri()));
        bind(ProductFinder.class).in(Singleton.class);
        bind(PaymentFinder.class).in(Singleton.class);
        bind(ProductApiTokenManager.class).in(Singleton.class);

        bind(PublicApiRestClient.class).toInstance(
                new PublicApiRestClient(client, configuration.getPublicApiUrl()));

        install(jpaModule(configuration));
        install(new FactoryModuleBuilder().build(ProductFactory.class));
        install(new FactoryModuleBuilder().build(PaymentFactory.class));
    }

    private JpaPersistModule jpaModule(ProductsConfiguration configuration) {
        DataSourceFactory dbConfig = configuration.getDataSourceFactory();
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", dbConfig.getDriverClass());
        properties.put("javax.persistence.jdbc.url", dbConfig.getUrl());
        if (dbConfig.getUser() != null) {
            properties.put("javax.persistence.jdbc.user", dbConfig.getUser());
        }
        if (dbConfig.getPassword() != null) {
            properties.put("javax.persistence.jdbc.password", dbConfig.getPassword());
        }

        JPAConfiguration jpaConfiguration = configuration.getJpaConfiguration();
        properties.put("eclipselink.logging.level", jpaConfiguration.getJpaLoggingLevel());
        properties.put("eclipselink.logging.level.sql", jpaConfiguration.getSqlLoggingLevel());
        properties.put("eclipselink.query-results-cache", jpaConfiguration.getCacheSharedDefault());
        properties.put("eclipselink.cache.shared.default", jpaConfiguration.getCacheSharedDefault());
        properties.put("eclipselink.ddl-generation.output-mode", jpaConfiguration.getDdlGenerationOutputMode());
        properties.put("eclipselink.session.customizer", ProductsSessionCustomiser.class.getCanonicalName());

        final JpaPersistModule jpaModule = new JpaPersistModule("ProductsUnit");
        jpaModule.properties(properties);

        return jpaModule;
    }

    @Provides
    @Singleton
    public Clock systemUtcClock() {
        return Clock.systemUTC();
    }

    @Provides
    @Singleton
    public ExpungeHistoricalDataConfig expungeHistoricalDataConfig() {
        return configuration.getExpungeHistoricalDataConfig();
    }
}
