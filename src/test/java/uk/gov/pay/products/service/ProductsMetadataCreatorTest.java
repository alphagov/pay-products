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
import uk.gov.pay.products.exception.ProductNotFoundException;
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
public class ProductsMetadataCreatorTest {

    @Mock
    private ProductDao mockProductDao;
    @Mock
    private ProductMetadataDao mockMetadataDao;
    @Captor
    private ArgumentCaptor<ProductMetadataEntity> persistedMetadataEntity;
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private ProductsMetadataCreator productsMetadataCreator;
    private String productExternalId = "externalId";
    private ProductEntity productEntity;

    @Before
    public void setUp() throws Exception {
        productsMetadataCreator = new ProductsMetadataCreator(mockProductDao, mockMetadataDao);
        productEntity = new ProductEntity();
        productEntity.setExternalId(productExternalId);
    }

    @Test
    public void shouldSuccess_whenProvidedWithAProductMetadataObject() {
        ProductMetadata metadata = new ProductMetadata(1, "key", "value");
        when(mockProductDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        ProductMetadata createdProductMetadata = productsMetadataCreator.createProductMetadata(metadata, productExternalId);
        assertThat(createdProductMetadata.getKey(), is("key"));

        assertThat(createdProductMetadata.getValue(), is("value"));
        verify(mockMetadataDao).persist(persistedMetadataEntity.capture());
        ProductMetadataEntity metadataEntity = persistedMetadataEntity.getValue();

        assertThat(metadataEntity.getMetadataKey(), is("key"));
        assertThat(metadataEntity.getMetadataValue(), is("value"));
    }

    @Test
    public void shouldSuccess_whenProvidedWithAJsonPayload() {
        JsonNode payload = new ObjectMapper()
                .valueToTree(
                        ImmutableMap.<String, String>builder()
                                .put("key", "value")
                                .build());
        when(mockProductDao.findByExternalId(productExternalId)).thenReturn(Optional.of(productEntity));
        ProductMetadata createdProductMetadata = productsMetadataCreator.createProductMetadata(payload, productExternalId);
        assertThat(createdProductMetadata.getKey(), is("key"));

        assertThat(createdProductMetadata.getValue(), is("value"));
        verify(mockMetadataDao).persist(persistedMetadataEntity.capture());
        ProductMetadataEntity metadataEntity = persistedMetadataEntity.getValue();

        assertThat(metadataEntity.getMetadataKey(), is("key"));
        assertThat(metadataEntity.getMetadataValue(), is("value"));
    }

    @Test
    public void shouldNotSuccess_whenProductNotFound() {
        ProductMetadata metadata = new ProductMetadata(1, "key", "value");
        when(mockProductDao.findByExternalId(productExternalId)).thenReturn(Optional.empty());

        thrown.expect(ProductNotFoundException.class);
        productsMetadataCreator.createProductMetadata(metadata, productExternalId);
    }
}