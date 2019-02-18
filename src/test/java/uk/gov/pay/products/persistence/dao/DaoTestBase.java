package uk.gov.pay.products.persistence.dao;

import com.google.inject.persist.jpa.JpaPersistModule;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.commons.testing.db.PostgresDockerRule;
import uk.gov.pay.products.infra.GuicedTestEnvironment;
import uk.gov.pay.products.utils.DatabaseTestHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

class DaoTestBase {

    private static Logger logger = LoggerFactory.getLogger(DaoTestBase.class);

    @ClassRule
    public static PostgresDockerRule postgres = new PostgresDockerRule();

    static DatabaseTestHelper databaseHelper;
    static GuicedTestEnvironment env;

    @BeforeClass
    public static void setup() throws Exception {
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", postgres.getDriverClass());
        properties.put("javax.persistence.jdbc.url", postgres.getConnectionUrl());
        properties.put("javax.persistence.jdbc.user", postgres.getUsername());
        properties.put("javax.persistence.jdbc.password", postgres.getPassword());

        JpaPersistModule jpaModule = new JpaPersistModule("ProductsUnit");
        jpaModule.properties(properties);

        databaseHelper = new DatabaseTestHelper(new DBI(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword()));

        try (Connection connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword())) {
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.update("");
        }

        env = GuicedTestEnvironment.from(jpaModule).start();
    }

    @AfterClass
    public static void tearDown() {
        try (Connection connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword())) {
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.dropAll();
        } catch (Exception e) {
            logger.error("Error reverting migrations", e);
        }
        env.stop();
    }

}
