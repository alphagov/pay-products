package uk.gov.pay.products.service;

public interface ProductFactory {
    ProductCreator productsCreator();

    ProductFinder productsFinder();
}
