package uk.gov.pay.products.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.products.model.ProductMetadata;
import uk.gov.pay.products.persistence.dao.ProductMetadataDao;
import uk.gov.pay.products.persistence.entity.ProductMetadataEntity;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.products.fixtures.ProductMetadataEntityFixture.aProductMetadataEntity;

@RunWith(MockitoJUnitRunner.class)
public class ProductsMetadataFinderTest {

    @Mock
    private ProductMetadataDao metadataDao;

    private ProductsMetadataFinder finder;

    @Before
    public void setUp() throws Exception {
        finder = new ProductsMetadataFinder(metadataDao);
    }

    @Test
    public void findByProductsExternalId_shouldReturnAListOfProductMetadata() {
        ProductMetadataEntity metadataEntity1 = aProductMetadataEntity()
                .withMetadataKey("key1")
                .withMetadataValue("value1")
                .build();

        ProductMetadataEntity metadataEntity2 = aProductMetadataEntity()
                .withMetadataKey("key2")
                .withMetadataValue("value2")
                .build();
        when(metadataDao.findByProductsExternalId("externalId")).thenReturn(List.of(metadataEntity1, metadataEntity2));
        List<ProductMetadata> metadataList = finder.findMetadataByProductExternalId("externalId");
        assertThat(metadataList.size(), is(2));
        assertThat(metadataList.get(0).getKey(), is("key1"));
        assertThat(metadataList.get(0).getValue(), is("value1"));
        assertThat(metadataList.get(1).getKey(), is("key2"));
        assertThat(metadataList.get(1).getValue(), is("value2"));
    }
}
