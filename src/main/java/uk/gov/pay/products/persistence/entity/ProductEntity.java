package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.util.ProductStatus;

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

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "catalogue_id", updatable = false)
    private CatalogueEntity catalogueEntity;

    @Column(name = "return_url")
    private String returnUrl;

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

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public CatalogueEntity getCatalogueEntity() {
        return catalogueEntity;
    }

    public void setCatalogueEntity(CatalogueEntity catalogueEntity) {
        this.catalogueEntity = catalogueEntity;
    }

    public static ProductEntity from(Product product) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.setStatus(product.getStatus());
        productEntity.setPrice(product.getPrice());
        productEntity.setName(product.getName());
        productEntity.setPayApiToken(product.getPayApiToken());
        productEntity.setExternalId(product.getExternalId());
        productEntity.setDescription(product.getDescription());
        productEntity.setReturnUrl(product.getReturnUrl());

        return productEntity;
    }

    public Product toProduct() {
        return new Product(
                this.externalId,
                this.catalogueEntity.getExternalServiceId(),
                this.name,
                this.description,
                this.payApiToken,
                this.price,
                this.status,
                this.catalogueEntity.getExternalId(),
                this.returnUrl);
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
}
