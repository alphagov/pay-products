package uk.gov.pay.products.extension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.pay.products.ProductsApplication;
import uk.gov.pay.products.config.PersistenceServiceInitialiser;
import uk.gov.pay.products.config.ProductsConfiguration;
import uk.gov.pay.products.config.ProductsModule;

import java.sql.Connection;
import java.sql.SQLException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.sql.DriverManager.getConnection;

public class ProductsAppWithPostgresExtension implements BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    private static final Logger logger = LoggerFactory.getLogger(ProductsAppWithPostgresExtension.class);
    private static final PostgreSQLContainer<?> postgresContainer;
    private static final String DB_USERNAME = "test";
    private static final String DB_PASSWORD = "test"; // pragma: allowlist secret
    
    private final DropwizardAppExtension<ProductsConfiguration> dropwizardAppExtension;
    public final WireMockServer publicApi = new WireMockServer(wireMockConfig().dynamicPort().bindAddress("localhost"));
    public final WireMockServer publicAuth = new WireMockServer(wireMockConfig().dynamicPort().bindAddress("localhost"));
    private Injector injector;

    public ProductsAppWithPostgresExtension() {
        publicApi.start();
        publicAuth.start();
        this.dropwizardAppExtension = new DropwizardAppExtension<>(
                ProductsApplication.class,
                resourceFilePath("config/test-it-config.yaml"),
                config("database.url", postgresContainer.getJdbcUrl()),
                config("database.user", DB_USERNAME),
                config("database.password", DB_PASSWORD),
                config("publicApiUrl", "http://localhost:" + publicApi.port()),
                config("publicAuthUrl", "http://localhost:" + publicAuth.port()));

        try {
            // starts dropwizard application. This is required as we don't use DropwizardExtensionsSupport (which starts application)
            // due to config overrides we need at runtime for database, sqs and any custom configuration needed for tests
            dropwizardAppExtension.before();
        } catch (Exception e) {
            logger.error("Exception starting application - {}", e.getMessage());
            throw new RuntimeException(e);
        }
        
        injector = Guice.createInjector(new ProductsModule(dropwizardAppExtension.getConfiguration(), dropwizardAppExtension.getEnvironment()));
        injector.getInstance(PersistenceServiceInitialiser.class);
    }

    public <T> T getInstanceFromGuiceContainer(Class<T> klazz) {
        return injector.getInstance(klazz);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (context.getRequiredTestClass().getEnclosingClass() == null) {
            publicApi.stop();
            publicAuth.stop();
            dropwizardAppExtension.after();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        publicAuth.resetAll();
        publicApi.resetAll();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (context.getRequiredTestClass().getEnclosingClass() == null) {
            // Only runs if there is no enclosing class, i.e. not in a @Nested block
            dropwizardAppExtension.getApplication().run("db", "migrate", resourceFilePath("config/test-it-config.yaml"));
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {}

    public int getPort() {
        return dropwizardAppExtension.getLocalPort();
    }

    static {
        final String databaseName = "products";

        postgresContainer = new PostgreSQLContainer<>("postgres:15.2")
                .withUsername(DB_USERNAME)
                .withPassword(DB_PASSWORD);

        postgresContainer.start();

        try (Connection connection = getConnection(postgresContainer.getJdbcUrl(), DB_USERNAME, DB_PASSWORD)) {
            connection.createStatement().execute("CREATE DATABASE " + databaseName + " WITH owner=" + DB_USERNAME + " TEMPLATE postgres");
            connection.createStatement().execute("GRANT ALL PRIVILEGES ON DATABASE " + databaseName + " TO " + DB_USERNAME);
            connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS pg_trgm");
            connection.createStatement().execute("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
