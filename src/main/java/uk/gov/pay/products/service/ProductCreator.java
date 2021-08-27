package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductUpdateRequest;
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
    public Optional<Product> doUpdateByGatewayAccountId(Integer gatewayAccountId, String productExternalId, ProductUpdateRequest productUpdateRequest) {

        Optional<ProductEntity> productEntityUpdated = productDao
                .findByGatewayAccountIdAndExternalId(gatewayAccountId, productExternalId)
                .map(productEntity -> {
                    productEntity.setName(productUpdateRequest.getName());
                    productEntity.setDescription(productUpdateRequest.getDescription());
                    productEntity.setPrice(productUpdateRequest.getPrice());
                    productEntity.setReferenceEnabled(productUpdateRequest.getReferenceEnabled());
                    productEntity.setReferenceLabel(productUpdateRequest.getReferenceLabel());
                    productEntity.setReferenceHint(productUpdateRequest.getReferenceHint());

                    return productEntity;
                });

        productMetadataDao.deleteForProductExternalId(productExternalId);

        productEntityUpdated.ifPresent(productEntity -> {
            if (productUpdateRequest.getMetadata() != null && !productUpdateRequest.getMetadata().isEmpty()) {
                List<ProductMetadataEntity> productMetadataEntities = productUpdateRequest.getMetadata()
                        .stream()
                        .map(productMetadata -> ProductMetadataEntity.from(productEntity, productMetadata))
                        .collect(Collectors.toList());
                productEntity.setMetadataEntityList(productMetadataEntities);
            }
        });

        return productEntityUpdated.map(productEntity -> linksDecorator.decorate(productEntity.toProduct()));
    }
}
