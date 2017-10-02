package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;

import java.util.Optional;

public class ProductFinder {
    private final ProductDao productDao;
    private final LinksDecorator linksDecorator;

    @Inject
    public ProductFinder(ProductDao productDao, LinksDecorator linksDecorator) {
        this.productDao = productDao;
        this.linksDecorator = linksDecorator;
    }

    @Transactional
    public Optional<Product> findByExternalId(String externalId) {
        return productDao.findByExternalId(externalId)
                .map(productEntity -> linksDecorator.decorate(productEntity.toProduct()));
    }
}
