package uk.gov.pay.products.service;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.model.ProductUsageStat;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.util.ProductStatus;
import uk.gov.pay.products.util.ProductType;

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
                .map(ProductEntity::toProduct)
                .map(linksDecorator::decorate);
    }

    @Transactional
    public Optional<Product> findByGatewayAccountIdAndExternalId(Integer gatewayAccountId, String externalId) {
        return productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)
                .map(ProductEntity::toProduct)
                .map(linksDecorator::decorate);
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
    public Optional<Product> updatePayApiTokenByExternalId(String externalId, String payApiToken) {
        return productDao.findByExternalId(externalId)
                .map(productEntity -> {
                    productEntity.setPayApiToken(payApiToken);
                    productDao.merge(productEntity);
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

    @Deprecated
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
    public Boolean deleteByGatewayAccountIdAndExternalId(Integer gatewayAccountId, String externalId) {
        return productDao.findByGatewayAccountIdAndExternalId(gatewayAccountId, externalId)
                .map(productEntity -> {
                    productDao.remove(productEntity);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public List<Product> findByGatewayAccountId(Integer gatewayAccountId) {
        return productDao.findByGatewayAccountId(gatewayAccountId)
                .stream()
                .map(ProductEntity::toProduct)
                .map(linksDecorator::decorate)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public List<Product> findByGatewayAccountIdAndType(Integer gatewayAccountId, ProductType type) {
        return productDao.findByGatewayAccountIdAndType(gatewayAccountId, type)
                .stream()
                .map(ProductEntity::toProduct)
                .map(linksDecorator::decorate)
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public Optional<Product> findByProductPath(String serviceNamePath, String productNamePath) {
        return productDao.findByProductPath(serviceNamePath, productNamePath)
                .map(ProductEntity::toProduct)
                .map(linksDecorator::decorate);
    }

    @Transactional
    public List<ProductUsageStat> findProductsAndUsage(Integer gatewayAccountId) {
        return productDao.findProductsAndUsage(gatewayAccountId)
                .stream()
                .peek(productUsageStat -> productUsageStat.setProduct(linksDecorator.decorate(productUsageStat.getProduct())))
                .collect(Collectors.toUnmodifiableList());
    }

    @Transactional
    public List<ProductUsageStat> findUnusedProducts(Integer gatewayAccountId) {
        return productDao.findUnusedProducts(gatewayAccountId)
                .stream()
                .peek(productUsageStat -> productUsageStat.setProduct(linksDecorator.decorate(productUsageStat.getProduct())))
                .collect(Collectors.toUnmodifiableList());
    }
}
