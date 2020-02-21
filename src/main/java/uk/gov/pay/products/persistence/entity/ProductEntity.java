package uk.gov.pay.products.persistence.entity;

import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.SupportedLanguageJpaConverter;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Column(name = "service_name_path")
    private String serviceNamePath;

    @Column(name = "product_name_path")
    private String productNamePath;
    
    @Column(name = "reference_enabled")
    private Boolean referenceEnabled;
    
    @Column(name = "reference_label")
    private String referenceLabel;
    
    @Column(name = "reference_hint")
    private String referenceHint;
    
    @Column(name = "language", nullable = false)
    @Convert(converter = SupportedLanguageJpaConverter.class)
    private SupportedLanguage language;

    @OneToMany(mappedBy = "productEntity", targetEntity = ProductMetadataEntity.class)
    private List<ProductMetadataEntity> metadataEntityList;

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

    public List<ProductMetadataEntity> getMetadataEntityList() {
        return metadataEntityList;
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

    public Boolean getReferenceEnabled() { return referenceEnabled; }

    public void setReferenceEnabled(Boolean referenceEnabled) { this.referenceEnabled = referenceEnabled; }

    public String getReferenceLabel() { return referenceLabel; }

    public void setReferenceLabel(String referenceLabel) { this.referenceLabel = referenceLabel; }

    public String getReferenceHint() { return referenceHint; }

    public void setReferenceHint(String referenceHint) { this.referenceHint = referenceHint; }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getServiceNamePath() { return serviceNamePath; }

    public void setServiceNamePath(String serviceNamePath) { this.serviceNamePath = serviceNamePath; }

    public String getProductNamePath() { return productNamePath; }

    public void setProductNamePath(String productNamePath) { this.productNamePath = productNamePath; }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
    }

    public void setMetadataEntityList(List<ProductMetadataEntity> metadataEntityList) {
        this.metadataEntityList = metadataEntityList;
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
        productEntity.setServiceNamePath(product.getServiceNamePath());
        productEntity.setProductNamePath(product.getProductNamePath());
        productEntity.setReferenceEnabled(product.getReferenceEnabled());
        productEntity.setReferenceLabel(product.getReferenceLabel());
        productEntity.setReferenceHint(product.getReferenceHint());
        productEntity.setLanguage(product.getLanguage());

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
                this.type,
                this.returnUrl,
                this.serviceNamePath,
                this.productNamePath,
                this.referenceEnabled,
                this.referenceLabel,
                this.referenceHint,
                this.language, 
                this.metadataEntityList == null ? null : this.metadataEntityList
                        .stream()
                        .map(ProductMetadata::from)
                        .collect(Collectors.toList()));
    }

    public Map<String, String> toProductMetadataMap() {
        return metadataEntityList == null ? Map.of() : metadataEntityList
                .stream()
                .collect(Collectors.toMap(metadata -> metadata.getMetadataKey(), metadata -> metadata.getMetadataValue()));
    }
}
