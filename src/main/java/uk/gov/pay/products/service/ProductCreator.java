package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import static uk.gov.pay.commons.utils.RandomIdGenerator.randomUuid;


public class ProductCreator {

    private final ProductDao productDao;
    private final LinksDecorator linksDecorator;

    @Inject
    public ProductCreator(ProductDao productDao, LinksDecorator linksDecorator) {
        this.productDao = productDao;
        this.linksDecorator = linksDecorator;
    }

    @Transactional
    public Product doCreate(Product product) {
        ProductEntity productEntity = ProductEntity.from(product);
        productEntity.setExternalId(randomUuid());

        productDao.persist(productEntity);

        return linksDecorator.decorate(productEntity.toProduct());
    }
}
