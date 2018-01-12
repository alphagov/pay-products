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
    public void setUp() throws Exception {
        updater = new GatewayAccountUpdater(productDao);
    }

    @Test
    public void shouldUpdateServiceName_whenSingleProduct() throws Exception {
        Integer gatewayAccountId = 1000;
        String serviceName = "New Service Name";
        PatchRequest request = PatchRequest.from(new ObjectMapper().valueToTree(ImmutableMap.of("op", "replace",
                "path", "notify_settings",
                "value", serviceName)));

        ProductEntity productEntity = mock(ProductEntity.class);
        when(productDao.findByGatewayAccountId(gatewayAccountId)).thenReturn(Arrays.asList(productEntity));

        Boolean success = updater.doPatch(gatewayAccountId, request);
        assertThat(success, is(true));
        verify(productDao, times(1)).merge(productEntity);
        verify(productEntity, times(1)).setServiceName(serviceName);
    }

    @Test
    public void shouldUpdateServiceName_whenTwoProduct() throws Exception {
        Integer gatewayAccountId = 1000;
        String serviceName = "New Service Name";
        PatchRequest request = PatchRequest.from(new ObjectMapper().valueToTree(ImmutableMap.of("op", "replace",
                "path", "notify_settings",
                "value", serviceName)));

        ProductEntity productEntity1 = mock(ProductEntity.class);
        ProductEntity productEntity2 = mock(ProductEntity.class);
        when(productDao.findByGatewayAccountId(gatewayAccountId)).thenReturn(Arrays.asList(productEntity1, productEntity2));

        Boolean success = updater.doPatch(gatewayAccountId, request);
        assertThat(success, is(true));
        verify(productDao, times(2)).merge(any(ProductEntity.class));
        verify(productEntity1, times(1)).setServiceName(serviceName);
        verify(productEntity2, times(1)).setServiceName(serviceName);
    }

    @Test
    public void shouldNotUpdateServiceName_whenNoProduct() throws Exception {
        Integer gatewayAccountId = 1000;
        String serviceName = "New Service Name";
        PatchRequest request = PatchRequest.from(new ObjectMapper().valueToTree(ImmutableMap.of("op", "replace",
                "path", "notify_settings",
                "value", serviceName)));

        when(productDao.findByGatewayAccountId(gatewayAccountId)).thenReturn(Arrays.asList());
        Boolean success = updater.doPatch(gatewayAccountId, request);
        assertThat(success, is(false));
        verify(productDao, times(0)).merge(any(ProductEntity.class));
    }
}
