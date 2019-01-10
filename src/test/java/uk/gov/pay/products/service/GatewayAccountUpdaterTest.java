package uk.gov.pay.products.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.products.model.PatchRequest;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GatewayAccountUpdaterTest {

    private ProductDao productDao = mock(ProductDao.class);
    private GatewayAccountUpdater updater;

    @Before
    public void setUp() {
        updater = new GatewayAccountUpdater(productDao);
    }

    @Test
    public void shouldUpdateServiceName() {
        Integer gatewayAccountId = 1000;
        String newServiceName = "New Service Name";
        PatchRequest request = PatchRequest.from(new ObjectMapper().valueToTree(ImmutableMap.of("op", "replace",
                "path", "service_name",
                "value", newServiceName)));

        when(productDao.updateGatewayAccount(gatewayAccountId, newServiceName)).thenReturn(2);

        Boolean success = updater.doPatch(gatewayAccountId, request);
        assertThat(success, is(true));
    }

    @Test
    public void shouldNotUpdateServiceName_whenNoProduct() {
        Integer gatewayAccountId = 1000;
        String serviceName = "New Service Name";
        PatchRequest request = PatchRequest.from(new ObjectMapper().valueToTree(ImmutableMap.of("op", "replace",
                "path", "service_name",
                "value", serviceName)));

        when(productDao.findByGatewayAccountId(gatewayAccountId)).thenReturn(Arrays.asList());
        Boolean success = updater.doPatch(gatewayAccountId, request);
        assertThat(success, is(false));
    }
}
