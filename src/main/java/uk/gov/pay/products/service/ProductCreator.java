package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ProductCreator {

    private final ProductDao productDao;
    private final ProductMetadataDao productMetadataDao;
    private final LinksDecorator linksDecorator;

    @Inject
    public ProductCreator(ProductDao productDao, ProductMetadataDao productMetadataDao, LinksDecorator linksDecorator) {
        this.productDao = productDao;
        this.productMetadataDao = productMetadataDao;
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

        Optional<ProductEntity> productEntityUpdated = productDao
                .findByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(productEntity -> {
                    productEntity.setName(product.getName());
                    productEntity.setDescription(product.getDescription());
                    productEntity.setPrice(product.getPrice());
                    productEntity.setReferenceEnabled(product.getReferenceEnabled());
                    productEntity.setReferenceLabel(product.getReferenceLabel());
                    productEntity.setReferenceHint(product.getReferenceHint());

                    return productEntity;
                });

        productMetadataDao.deleteForProductExternalId(productExternalId);

        productEntityUpdated.ifPresent(productEntity -> {
            if (product.getMetadata() != null && !product.getMetadata().isEmpty()) {
                List<ProductMetadataEntity> productMetadataEntities = product.getMetadata()
                        .stream()
                        .map(productMetadata -> ProductMetadataEntity.from(productEntity, productMetadata))
                        .collect(Collectors.toList());
                productEntity.setMetadataEntityList(productMetadataEntities);
            }
        });

        return productEntityUpdated.map(productEntity -> linksDecorator.decorate(productEntity.toProduct()));
    }
}
