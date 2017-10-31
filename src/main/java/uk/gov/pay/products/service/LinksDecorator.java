package uk.gov.pay.products.service;

import uk.gov.pay.products.model.Link;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.model.Product;

import java.net.URI;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.UriBuilder.fromUri;
import static uk.gov.pay.products.resources.PaymentResource.PAYMENTS_RESOURCE;
import static uk.gov.pay.products.resources.ProductResource.PRODUCTS_RESOURCE;

public class LinksDecorator {

    private final String productsBaseUrl;
    private final String productsUIUrl;

    public LinksDecorator(String productsBaseUrl, String productsUIUrl) {
        this.productsBaseUrl = productsBaseUrl;
        this.productsUIUrl = productsUIUrl;
    }

    public Product decorate(Product product) {
        Link selfLink = makeSelfLink(GET, PRODUCTS_RESOURCE, product.getExternalId());
        product.getLinks().add(selfLink);

        Link payLink = makeProductsUIUri(POST, product.getExternalId());
        product.getLinks().add(payLink);

        return product;
    }

    public Payment decorate(Payment payment){
        Link selfLink = makeSelfLink(GET, PAYMENTS_RESOURCE , payment.getExternalId());
        payment.getLinks().add(selfLink);

        return payment;
    }

    private Link makeSelfLink(String method, String resourcePath, String externalId){
        URI uri = fromUri(productsBaseUrl).path(resourcePath).path(externalId).build();
        Link selfLink = Link.from(Link.Rel.self, method, uri.toString());

        return selfLink;
    }

    private Link makeProductsUIUri(String method, String externalId){
        URI productsUIUri = fromUri(productsUIUrl).path(externalId).build();
        Link payLink = Link.from(Link.Rel.pay, method, productsUIUri.toString());

        return payLink;
    }
}
