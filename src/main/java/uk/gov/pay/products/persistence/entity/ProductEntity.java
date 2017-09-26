package uk.gov.pay.products.persistence.entity;

import javax.persistence.*;
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

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "price")
    private Long price;

    @Column(name = "status")
    private String status;

    @Column(name = "date_created")
    @Convert(converter = UTCDateTimeConverter.class)
    private ZonedDateTime dateCreated;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "catalogue_id", updatable = false)
    private CatalogueEntity catalogueEntity;

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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
}
