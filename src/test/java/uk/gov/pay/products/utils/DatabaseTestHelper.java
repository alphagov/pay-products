package uk.gov.pay.products.utils;

import org.skife.jdbi.v2.DBI;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

public class DatabaseTestHelper {

    private DBI jdbi;

    public DatabaseTestHelper(DBI jdbi) {
        this.jdbi = jdbi;
    }

    public DatabaseTestHelper addProduct(ProductEntity productEntity) {
        addCatalogue(productEntity.getCatalogueEntity());

        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO products " +
                "(catalogue_id, external_id, name, description, pay_api_token, price, " +
                "status, return_url)" +
                "VALUES " +
                "(:catalogue_id, :external_id, :name, :description, :pay_api_token, :price, " +
                ":status, :return_url)")
                .bind("catalogue_id", productEntity.getCatalogueEntity().getId())
                .bind("external_id", productEntity.getExternalId())
                .bind("name", productEntity.getName())
                .bind("description", productEntity.getDescription())
                .bind("pay_api_token", productEntity.getPayApiToken())
                .bind("price", productEntity.getPrice())
                .bind("status", productEntity.getStatus())
                .bind("return_url", productEntity.getReturnUrl())
                .execute());

        return this;
    }

    public DatabaseTestHelper addCatalogue(CatalogueEntity catalogueEntity) {
        jdbi.withHandle(handle -> handle.createStatement("INSERT INTO catalogues " +
                "(external_id, external_service_id, status)" +
                "VALUES (:external_id, :external_service_id, :status)")
                .bind("external_id", catalogueEntity.getExternalId())
                .bind("external_service_id", catalogueEntity.getExternalServiceId())
                .bind("status", catalogueEntity.getStatus())
                .execute());

        return this;
    }

}
