package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import java.time.ZonedDateTime;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductEntityFixture {

    private String description = "default description";
    private String apiKey = "default api key";
    private String name = "default name";
    private String serviceName = "default service";
    private Long price = 100L;
    private String returnUrl = "https://return.url";
    private ProductStatus status = ProductStatus.ACTIVE;
    private ProductType type = ProductType.DEMO;
    private String externalId = randomUuid();
    private ZonedDateTime dateCreated = ZonedDateTime.now();
    private int gatewayAccountId;
    private String serviceNamePath;
    private String productNamePath;

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
        product.setType(type);
        product.setGatewayAccountId(gatewayAccountId);
        product.setReturnUrl(returnUrl);
        product.setServiceName(serviceName);
        product.setServiceNamePath(serviceNamePath);
        product.setProductNamePath(productNamePath);

        return product;
    }

    public static ProductEntityFixture aProductEntity() {
        return new ProductEntityFixture();
    }

    public ProductEntityFixture withName(String name) {
        this.name = name;
        return this;
    }

    public ProductEntityFixture withDescription(String description) {
        this.description = description;
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

    public ProductEntityFixture withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ProductEntityFixture withType(ProductType type) {
        this.type = type;
        return this;
    }

    public ProductEntityFixture withReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }

    public ProductEntityFixture withProductPath(String serviceNamePath, String productNamePath) {
        this.serviceNamePath = serviceNamePath;
        this.productNamePath = productNamePath;
        return this;
    }

    public ProductEntityFixture withPrice(long price) {
        this.price = price;
        return this;
    }
}
