package uk.gov.pay.products.service;

import uk.gov.pay.products.model.Link;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.model.Product;

import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.UriBuilder.fromUri;

public class LinksDecorator {

    private final String productsBaseUrl;
    private final String productsUIUrl;

    public LinksDecorator(String productsBaseUrl, String productsUIUrl) {
        this.productsBaseUrl = productsBaseUrl;
        this.productsUIUrl = productsUIUrl;
    }

    public Product decorate(Product product) {
        Link selfLink = makeSelfLink(GET, "v1/api/products", product.getExternalId());
        product.getLinks().add(selfLink);

        Link payLink = makeProductsUIUri(POST, product.getExternalId());
        product.getLinks().add(payLink);

        return product;
    }

    public Payment decorate(Payment payment){
        Link selfLink = makeSelfLink(GET, "v1/api/payments", payment.getExternalId());
        payment.getLinks().add(selfLink);

        Link nextUrl = makeNextUrlLink(GET, payment.getNextUrl());
        payment.getLinks().add(nextUrl);

        return payment;
    }

    private Link makeSelfLink(String method, String resourcePath, String externalId){
        URI uri = fromUri(productsBaseUrl).path(resourcePath).path(externalId).build();
        return Link.from(Link.Rel.self, method, uri.toString());
    }

    private Link makeNextUrlLink(String method, String nextUrl){
        return Link.from(Link.Rel.next, method, nextUrl);
    }

    private Link makeProductsUIUri(String method, String externalId){
        URI productsUIUri = fromUri(productsUIUrl).path(externalId).build();
        return Link.from(Link.Rel.pay, method, productsUIUri.toString());
    }
}
