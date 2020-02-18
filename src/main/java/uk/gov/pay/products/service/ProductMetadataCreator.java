package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.exception.ProductNotFoundException;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import static java.lang.String.format;

public class ProductMetadataCreator {

    private ProductDao productDao;
    private ProductMetadataDao metadataDao;

    @Inject
    public ProductMetadataCreator(ProductDao productDao, ProductMetadataDao metadataDao) {
        this.productDao = productDao;
        this.metadataDao = metadataDao;
    }

    @Transactional
    public ProductMetadata createProductMetadata(ProductMetadata metadata, String productExternalId) {
        return productDao.findByExternalId(productExternalId)
                .map(productEntity -> {
                    ProductMetadataEntity metadataEntity = new ProductMetadataEntity();
                    metadataEntity.setProductEntity(productEntity);
                    metadataEntity.setMetadataKey(metadata.getKey());
                    metadataEntity.setMetadataValue(metadata.getValue());
                    metadataDao.persist(metadataEntity);
                    return metadataEntity.toMetadata();
                }).orElseThrow(() -> new ProductNotFoundException(format("Product with %s id not found", productExternalId), productExternalId));
    }
}
