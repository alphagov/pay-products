package uk.gov.pay.products.healthchecks;

import io.dropwizard.db.DataSourceFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ApplicationStartupDependentResource {

    private final DataSourceFactory dataSourceFactory;

    @Inject
    public ApplicationStartupDependentResource(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    public Connection getDatabaseConnection() throws SQLException {
        return DriverManager.getConnection(
                dataSourceFactory.getUrl(),
                dataSourceFactory.getUser(),
                dataSourceFactory.getPassword());
    }

    public void sleep(long durationSeconds) {
        try {
            Thread.sleep(durationSeconds);
        } catch (InterruptedException ignored) {}
    }

}
