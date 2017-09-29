package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductsCreator {

    private final ProductDao productDao;
    private final LinksDecorator linksDecorator;

    @Inject
    public ProductsCreator(ProductDao productDao, LinksDecorator linksDecorator) {
        this.productDao = productDao;
        this.linksDecorator = linksDecorator;
    }

    @Transactional
    public Product doCreate(Product product) {
        CatalogueEntity catalogueEntity = new CatalogueEntity();
        catalogueEntity.setExternalId(randomUuid());
        catalogueEntity.setExternalServiceId(product.getExternalServiceId());

        ProductEntity productEntity = ProductEntity.from(product);
        productEntity.setExternalId(randomUuid());
        productEntity.setCatalogueEntity(catalogueEntity);

        productDao.persist(productEntity);

        return linksDecorator.decorate(productEntity.toProduct());
    }
}
