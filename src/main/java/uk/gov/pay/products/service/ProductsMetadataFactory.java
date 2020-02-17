package uk.gov.pay.products.service;

public interface ProductsMetadataFactory {
    ProductsMetadataFinder metadataFinder();
    ProductMetadataCreator metadataCreator();
}
