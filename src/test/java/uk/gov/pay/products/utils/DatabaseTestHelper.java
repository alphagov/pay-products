package uk.gov.pay.products.utils;

import org.skife.jdbi.v2.DBI;
import uk.gov.pay.products.model.Product;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public DatabaseTestHelper addProduct(Product product) {
        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO products " +
                "(external_id, name, description, pay_api_token, price, " +
                "status, return_url, gateway_account_id)" +
                "VALUES " +
                "(:external_id, :name, :description, :pay_api_token, :price, " +
                ":status, :return_url, :gateway_account_id)")
                .bind("external_id", product.getExternalId())
                .bind("name", product.getName())
                .bind("description", product.getDescription())
                .bind("pay_api_token", product.getPayApiToken())
                .bind("price", product.getPrice())
                .bind("status", product.getStatus())
                .bind("return_url", product.getReturnUrl())
                .bind("gateway_account_id", product.getGatewayAccountId())
                .execute());

        return this;
    }
}
