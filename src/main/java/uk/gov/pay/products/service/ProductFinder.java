package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Transactional
    public Optional<Product> findByGatewayAccountIdAndExternalId(Integer gatewayAccountId, String externalId) {
        return productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)
                .map(productEntity -> linksDecorator.decorate(productEntity.toProduct()));
    }

    @Deprecated
    @Transactional
    public Optional<Product> disableByExternalId(String externalId) {
        return productDao.findByExternalId(externalId)
                .map(productEntity -> {
                    productEntity.setStatus(ProductStatus.INACTIVE);
                    return Optional.of(productEntity.toProduct());
                })
                .orElseGet(Optional::empty);
    }

    @Transactional
    public Boolean deleteByExternalId(String externalId) {
        return productDao.findByExternalId(externalId)
                .map(productEntity -> {
                    productDao.remove(productEntity);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<Product> disableByGatewayAccountIdAndExternalId(Integer gatewayAccountId, String externalId) {
        return productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)
                .map(productEntity -> {
                    productEntity.setStatus(ProductStatus.INACTIVE);
                    return Optional.of(productEntity.toProduct());
                })
                .orElseGet(Optional::empty);
    }

    @Transactional
    public List<Product> findByGatewayAccountId(Integer gatewayAccountId) {
        return productDao.findByGatewayAccountId(gatewayAccountId)
                .stream()
                .map(ProductEntity::toProduct)
                .collect(Collectors.toList())
                .stream()
                .map(product -> linksDecorator.decorate(product))
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<Product> findByProductPath(String serviceNamePath, String productNamePath) {
        return productDao.findByProductPath(serviceNamePath, productNamePath)
                .map(productEntity -> linksDecorator.decorate(productEntity.toProduct()));
    }
}
