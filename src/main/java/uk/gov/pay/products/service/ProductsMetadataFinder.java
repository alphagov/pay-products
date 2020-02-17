package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.List;
import java.util.stream.Collectors;

public class ProductsMetadataFinder {

    private final ProductMetadataDao dao;

    @Inject
    public ProductsMetadataFinder(ProductMetadataDao dao) {
        this.dao = dao;
    }

    @Transactional
    public List<ProductMetadata> findMetadataByProductId(Integer productId) {
        return dao.findByProductsId(productId)
                .stream()
                .map(ProductMetadataEntity::toMetadata)
                .collect(Collectors.toList());
    }
}
