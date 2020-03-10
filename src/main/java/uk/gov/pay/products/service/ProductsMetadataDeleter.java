package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.exception.MetadataNotFoundException;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;

public class ProductsMetadataDeleter {

    private ProductMetadataDao productMetadataDao;

    @Inject
    public ProductsMetadataDeleter(ProductMetadataDao productMetadataDao) {
        this.productMetadataDao = productMetadataDao;
    }

    @Transactional
    public void deleteMetadata(String productExternalId, String metadataKey) throws MetadataNotFoundException {
        productMetadataDao.findByProductsExternalIdAndKey(productExternalId, metadataKey)
                .ifPresentOrElse(productMetadataEntity -> productMetadataDao.remove(productMetadataEntity),
                        () -> { throw new MetadataNotFoundException(productExternalId, metadataKey); });
    }
}
