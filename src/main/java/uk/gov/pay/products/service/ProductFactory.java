package uk.gov.pay.products.service;

public interface ProductFactory {
    ProductCreator productCreator();

    ProductFinder productFinder();
}
