package uk.gov.pay.products.persistence.dao;

import com.google.inject.persist.jpa.JpaPersistModule;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jdbi.v3.core.Jdbi;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.commons.testing.db.PostgresDockerRule;
import uk.gov.pay.products.infra.GuicedTestEnvironment;
import uk.gov.pay.products.utils.DatabaseTestHelper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DaoTestBase {

    private static final Logger logger = LoggerFactory.getLogger(DaoTestBase.class);

    @ClassRule
    public static final PostgresDockerRule postgres = new PostgresDockerRule();

    static DatabaseTestHelper databaseHelper;
    static GuicedTestEnvironment env;

    @BeforeClass
    public static void setup() throws Exception {
        System.out.println("DaoTestBase setup");
        final Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.driver", postgres.getDriverClass());
        properties.put("javax.persistence.jdbc.url", postgres.getConnectionUrl());
        properties.put("javax.persistence.jdbc.user", postgres.getUsername());
        properties.put("javax.persistence.jdbc.password", postgres.getPassword());

        JpaPersistModule jpaModule = new JpaPersistModule("ProductsUnit");
        jpaModule.properties(properties);

        databaseHelper = new DatabaseTestHelper(Jdbi.create(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword()));

        try (Connection connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword())) {
            System.out.println("DaoTestBase setup new Migrator");
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.update("");
        }

        env = GuicedTestEnvironment.from(jpaModule).start();
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("DaoTestBase tearDown");
        try (Connection connection = DriverManager.getConnection(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword())) {
            System.out.println("DaoTestBase tearDown new Migrator");
            Liquibase migrator = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
            migrator.dropAll();
        } catch (Exception e) {
            logger.error("Error reverting migrations", e);
        }
        System.out.println("DaoTestBase env.stop()");
        env.stop();
    }

}
