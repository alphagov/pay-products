package uk.gov.pay.products.service;

import uk.gov.pay.products.model.Link;
import uk.gov.pay.products.model.Product;

import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static uk.gov.pay.products.resources.ProductResource.PRODUCTS_RESOURCE;

public class LinksBuilder {

    private final String productsBaseUrl;
    private final String productsUIUrl;

    public LinksBuilder(String productsBaseUrl, String productsUIUrl) {
        this.productsBaseUrl = productsBaseUrl;
        this.productsUIUrl = productsUIUrl;
    }

    public Product decorate(Product product) {
        URI uri = fromUri(productsBaseUrl).path(PRODUCTS_RESOURCE).path(product.getExternalId())
                .build();
        Link selfLink = Link.from(Link.Rel.self, GET, uri.toString());
        product.getLinks().add(selfLink);

        URI productsUIUri = fromUri(productsUIUrl).path(product.getExternalId())
                .build();
        Link payLink = Link.from(Link.Rel.pay, POST, productsUIUri.toString());
        product.getLinks().add(payLink);

        return product;
    }

}
