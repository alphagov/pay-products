package uk.gov.pay.products.service;

import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class GatewayAccountUpdater {

    private final ProductDao productDao;

    @Inject
    public GatewayAccountUpdater(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public Boolean doPatch(Integer gatewayAccountId, PatchRequest patchRequest) {
        List<ProductEntity> productEntities = productDao.findByGatewayAccountId(gatewayAccountId);
        productEntities
                .forEach(productEntity -> {
                    productEntity.setServiceName(patchRequest.valueAsString());
                    productDao.merge(productEntity);
                });

        return !productEntities.isEmpty();
    }
}
