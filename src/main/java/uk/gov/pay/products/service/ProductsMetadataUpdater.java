package uk.gov.pay.products.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.exception.MetadataNotFoundException;
import uk.gov.pay.products.exception.ProductNotFoundException;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;

public class ProductsMetadataUpdater {

    private ProductDao productDao;
    private ProductMetadataDao metadataDao;

    @Inject
    public ProductsMetadataUpdater(ProductDao productDao, ProductMetadataDao metadataDao) {
        this.productDao = productDao;
        this.metadataDao = metadataDao;
    }

    @Transactional
    public ProductMetadata updateMetadata(JsonNode payload, String productExternalId) {
        return productDao.findByExternalId(productExternalId)
                .map(productEntity -> {
                    String key = payload.fieldNames().next();
                    String value = payload.findValue(key).textValue();
                    return metadataDao.findByProductsExternalIdAndKey(productExternalId, key)
                            .map(productMetadataEntity -> {
                                productMetadataEntity.setMetadataValue(value);
                                metadataDao.merge(productMetadataEntity);
                                return productMetadataEntity.toMetadata();
                            })
                            .orElseThrow(() -> new MetadataNotFoundException(productExternalId, key));
                })
                .orElseThrow(() -> new ProductNotFoundException(productExternalId));
    }
}
