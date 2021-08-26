package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import java.time.ZonedDateTime;
import java.util.List;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductEntityFixture {

    private String description = "default description";
    private String name = "default name";
    private String apiToken = "default api token";
    private Long price = 100L;
    private String returnUrl = "https://return.url";
    private ProductStatus status = ProductStatus.ACTIVE;
    private ProductType type = ProductType.DEMO;
    private String externalId = randomUuid();
    private final ZonedDateTime dateCreated = ZonedDateTime.now();
    private int gatewayAccountId;
    private String serviceNamePath;
    private String productNamePath;
    private boolean referenceEnabled;
    private String referenceLabel;
    private String referenceHint;
    private SupportedLanguage language = SupportedLanguage.ENGLISH;
    private List<ProductMetadataEntity> productMetadataEntities;
    private boolean requireCaptcha;

    private ProductEntityFixture() { }

    public ProductEntity build() {
        ProductEntity product = new ProductEntity();
        product.setPayApiToken(apiToken);
        product.setDateCreated(dateCreated);
        product.setDescription(description);
        product.setName(name);
        product.setExternalId(externalId);
        product.setPrice(price);
        product.setStatus(status);
        product.setType(type);
        product.setGatewayAccountId(gatewayAccountId);
        product.setReturnUrl(returnUrl);
        product.setServiceNamePath(serviceNamePath);
        product.setProductNamePath(productNamePath);
        product.setReferenceEnabled(referenceEnabled);
        product.setReferenceLabel(referenceLabel);
        product.setReferenceHint(referenceHint);
        product.setLanguage(language);
        product.setMetadataEntityList(productMetadataEntities);
        product.setRequireCaptcha(requireCaptcha);

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

    public ProductEntityFixture withApiToken(String apiToken) {
        this.apiToken = apiToken;
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

    public ProductEntityFixture withType(ProductType type) {
        this.type = type;
        return this;
    }

    public ProductEntityFixture withReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }
    
    public ProductEntityFixture withReferenceEnabled(boolean referenceEnabled) {
        this.referenceEnabled = referenceEnabled;
        return this;
    }
    
    public ProductEntityFixture withReferenceLabel(String referenceLabel) {
        this.referenceLabel = referenceLabel;
        return this;
    }
    
    public ProductEntityFixture withReferenceHint(String referenceHint) {
        this.referenceHint = referenceHint;
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
    
    public ProductEntityFixture withLanguage(SupportedLanguage language) {
        this.language = language;
        return this;
    }

    public ProductEntityFixture withMetadata(List<ProductMetadataEntity> productMetadataEntities) {
        this.productMetadataEntities = productMetadataEntities;
        return this;
    }
    
    public ProductEntityFixture withRequireCaptcha(boolean requireCaptcha) {
        this.requireCaptcha = requireCaptcha;
        return this;
    }
}
