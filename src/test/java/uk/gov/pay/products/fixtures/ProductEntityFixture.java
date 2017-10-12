package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.time.ZonedDateTime;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductEntityFixture {

    private String description = "default description";
    private String apiKey = "default api key";
    private String name = "default name";
    private Long price = 100L;
    private ProductStatus status = ProductStatus.ACTIVE;
    private String externalId = randomUuid();
    private ZonedDateTime dateCreated = ZonedDateTime.now();
    private int gatewayAccountId;

    private ProductEntityFixture() { }

    public ProductEntity build() {
        ProductEntity product = new ProductEntity();
        product.setPayApiToken(apiKey);
        product.setDateCreated(dateCreated);
        product.setDescription(description);
        product.setName(name);
        product.setExternalId(externalId);
        product.setPrice(price);
        product.setStatus(status);
        product.setGatewayAccountId(gatewayAccountId);

        return product;
    }

    public static ProductEntityFixture aProductEntity() {
        return new ProductEntityFixture();
    }

    public ProductEntityFixture withName(String name) {
        this.name = name;
        return this;
    }

    public ProductEntityFixture withGatewayAccountId(int gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
        return this;
    }

    public ProductEntityFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public ProductEntityFixture withStatus(ProductStatus status) {
        this.status = status;
        return this;
    }
}
