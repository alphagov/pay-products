package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Optional;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

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

    @Transactional
    public Optional<Product> doUpdateByGatewayAccountId(Integer gatewayAccountId, String productExternalId, Product product) {

        return productDao
                .findByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(productEntity -> {
                    productEntity.setName(product.getName());
                    productEntity.setDescription(product.getDescription());
                    productEntity.setPrice(product.getPrice());
                    return linksDecorator.decorate(productEntity.toProduct());
                });
    }
}
