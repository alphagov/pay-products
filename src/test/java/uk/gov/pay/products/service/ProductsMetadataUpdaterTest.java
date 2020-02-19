package uk.gov.pay.products.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductsMetadataUpdaterTest {

    @Mock
    private ProductDao mockProductDao;
    @Mock
    private ProductMetadataDao mockMetadataDao;
    @Captor
    private ArgumentCaptor<ProductMetadataEntity> updatedMetadataEntity;
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private ProductsMetadataUpdater productsMetadataUpdater;
    private String productExternalId = "externalId";
    private ProductEntity productEntity;
    private ProductMetadataEntity metadataEntity;

    @Before
    public void setUp() throws Exception {
        productsMetadataUpdater = new ProductsMetadataUpdater(mockProductDao, mockMetadataDao);
        productEntity = new ProductEntity();
        productEntity.setExternalId(productExternalId);
        metadataEntity = new ProductMetadataEntity();
        metadataEntity.setMetadataKey("key");
        metadataEntity.setMetadataValue("value");
    }

    @Test
    public void shouldSuccess_whenProvidedWithAProductMetadataObject() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("key", "new value")
                                .build());

        when(mockProductDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        when(mockMetadataDao.findByProductsExternalIdAndKey(productExternalId, "key")).thenReturn(Optional.of(metadataEntity));
        ProductMetadata updatedMetadata = productsMetadataUpdater.updateMetadata(payload, productExternalId);
        assertThat(updatedMetadata.getKey(), is("key"));

        assertThat(updatedMetadata.getValue(), is("new value"));
        verify(mockMetadataDao).merge(updatedMetadataEntity.capture());
        ProductMetadataEntity metadataEntity = updatedMetadataEntity.getValue();

        assertThat(metadataEntity.getMetadataKey(), is("key"));
        assertThat(metadataEntity.getMetadataValue(), is("new value"));
    }
}