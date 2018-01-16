package uk.gov.pay.products.service;

import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static uk.gov.pay.products.validations.GatewayAccountRequestValidator.FIELD_SERVICE_NAME;

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
