package uk.gov.pay.products.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.CatalogueEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductsCreator {

    private final ProductDao productDao;
    private final LinksBuilder linksBuilder;

    @Inject
    public ProductsCreator(ProductDao productDao, LinksBuilder linksBuilder) {
        this.productDao = productDao;
        this.linksBuilder = linksBuilder;
    }

    @Transactional
    public Product doCreate(JsonNode payload) {
        Product product = Product.from(payload);

        CatalogueEntity catalogueEntity = new CatalogueEntity();
        catalogueEntity.setExternalId(randomUuid());
        catalogueEntity.setExternalServiceId(product.getExternalServiceId());

        ProductEntity productEntity = ProductEntity.from(product);
        productEntity.setExternalId(randomUuid());
        productEntity.setCatalogueEntity(catalogueEntity);

        productDao.persist(productEntity);

        return linksBuilder.decorate(productEntity.toProduct());
    }
}
