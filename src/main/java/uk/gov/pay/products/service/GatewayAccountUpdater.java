package uk.gov.pay.products.service;

import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.persistence.dao.ProductDao;

import javax.inject.Inject;

public class GatewayAccountUpdater {

    private final ProductDao productDao;

    @Inject
    public GatewayAccountUpdater(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public Boolean doPatch(Integer gatewayAccountId, PatchRequest patchRequest) {
        return productDao
                .updateGatewayAccount(gatewayAccountId, patchRequest.valueAsString()) > 0;
    }
}
