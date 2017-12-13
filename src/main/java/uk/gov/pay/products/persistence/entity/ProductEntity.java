package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import javax.persistence.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "products")
public class ProductEntity extends AbstractEntity {

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "pay_api_token")
    private String payApiToken;

    @Column(name = "price")
    private Long price;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "date_created")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime dateCreated = ZonedDateTime.now(ZoneId.of("UTC"));

    @Column(name = "gateway_account_id")
    private Integer gatewayAccountId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ProductType type = ProductType.DEMO;

    @Column(name = "return_url")
    private String returnUrl;

    @Column(name = "service_name")
    private String serviceName;

    public ProductEntity() {
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPayApiToken() {
        return payApiToken;
    }

    public void setPayApiToken(String payApiToken) {
        this.payApiToken = payApiToken;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getGatewayAccountId() {
        return gatewayAccountId;
    }

    public void setGatewayAccountId(Integer gatewayAccountId) {
        this.gatewayAccountId = gatewayAccountId;
    }

    public ProductType getType() {
        return type;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public static ProductEntity from(Product product) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.setStatus(product.getStatus());
        productEntity.setPrice(product.getPrice());
        productEntity.setName(product.getName());
        productEntity.setPayApiToken(product.getPayApiToken());
        productEntity.setExternalId(product.getExternalId());
        productEntity.setDescription(product.getDescription());
        productEntity.setGatewayAccountId(product.getGatewayAccountId());
        productEntity.setType(product.getType());
        productEntity.setReturnUrl(product.getReturnUrl());
        productEntity.setServiceName(product.getServiceName());

        return productEntity;
    }

    public Product toProduct() {
        return new Product(
                this.externalId,
                this.name,
                this.description,
                this.payApiToken,
                this.price,
                this.status,
                this.gatewayAccountId,
                this.serviceName,
                this.type,
                this.returnUrl);
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
