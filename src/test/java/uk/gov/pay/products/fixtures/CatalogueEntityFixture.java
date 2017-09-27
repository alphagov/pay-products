package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.util.ProductStatus;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class CatalogueEntityFixture {

    private String externalId = randomUuid();
    private String externalServiceId = "default external service id";
    private String name = "default name";
    private ProductStatus status = ProductStatus.ACTIVE;

    private CatalogueEntityFixture() {
    }

    public CatalogueEntity build() {
        CatalogueEntity catalogueEntity = new CatalogueEntity();
        catalogueEntity.setExternalId(externalId);
        catalogueEntity.setExternalServiceId(externalServiceId);
        catalogueEntity.setName(name);
        catalogueEntity.setStatus(status);

        return catalogueEntity;
    }

    public static CatalogueEntityFixture aCatalogueEntity() {
        return new CatalogueEntityFixture();
    }

    public CatalogueEntityFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CatalogueEntityFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public CatalogueEntityFixture withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }
}
