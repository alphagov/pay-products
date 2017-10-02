package uk.gov.pay.products.utils;

import org.skife.jdbi.v2.DBI;
import uk.gov.pay.products.model.Product;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public DatabaseTestHelper addProduct(Product product, int catalogueId) {

        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO catalogues " +
                "(id, external_id, external_service_id)" +
                "VALUES (:id, :external_id, :external_service_id)")
                .bind("id", catalogueId)
                .bind("external_id", product.getCatalogueExternalId())
                .bind("external_service_id", product.getExternalServiceId())
                .execute());

        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO products " +
                "(catalogue_id, external_id, name, description, pay_api_token, price, " +
                "status, return_url)" +
                "VALUES " +
                "(:catalogue_id, :external_id, :name, :description, :pay_api_token, :price, " +
                ":status, :return_url)")
                .bind("catalogue_id", catalogueId)
                .bind("external_id", product.getExternalId())
                .bind("name", product.getName())
                .bind("description", product.getDescription())
                .bind("pay_api_token", product.getPayApiToken())
                .bind("price", product.getPrice())
                .bind("status", product.getStatus())
                .bind("return_url", product.getReturnUrl())
                .execute());

        return this;
    }
}
