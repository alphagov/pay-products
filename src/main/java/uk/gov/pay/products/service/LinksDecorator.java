package uk.gov.pay.products.service;

import uk.gov.pay.products.model.Link;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.model.Product;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;
import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.core.UriBuilder.fromUri;

public class LinksDecorator {

    private final String productsBaseUrl;
    private final String productsUIUrl;
    private final String friendlyBaseUrl;

    public LinksDecorator(String productsBaseUrl, String productsUIUrl, String friendlyBaseUrl) {
        this.productsBaseUrl = productsBaseUrl;
        this.productsUIUrl = productsUIUrl;
        this.friendlyBaseUrl = friendlyBaseUrl;
    }

    public Product decorate(Product product) {
        Link selfLink = makeSelfLink(GET, "v1/api/products", product.getExternalId());
        product.getLinks().add(selfLink);

        Link payLink = makeProductsUIUri(GET, product.getExternalId(), product.getReferenceEnabled());
        product.getLinks().add(payLink);

        if(product.getServiceNamePath() != null && product.getProductNamePath() != null) {
            Link friendlyLink = makeFriendlyLink(GET, product.getServiceNamePath(), product.getProductNamePath());
            product.getLinks().add(friendlyLink);
        }

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

    private Link makeProductsUIUri(String method, String externalId, Boolean referenceEnabled){
        URI productsUIUri = referenceEnabled ?
                fromUri(productsUIUrl).path("reference").path(externalId).build() : fromUri(productsUIUrl).path(externalId).build();
        return Link.from(Link.Rel.pay, method, productsUIUri.toString());
    }

    private Link makeFriendlyLink(String method, String serviceNamePath, String productNamePath) {
        if(serviceNamePath == null || productNamePath == null){
            return null;
        }
        try {
            URI friendlyUri = fromUri(friendlyBaseUrl)
                .path(URLEncoder.encode(serviceNamePath, StandardCharsets.UTF_8.name()))
                .path(URLEncoder.encode(productNamePath, StandardCharsets.UTF_8.name()))
                .build();

            return Link.from(Link.Rel.friendly, method, friendlyUri.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(format("Friendly URL is not encodable [ %s %s ]", serviceNamePath, productNamePath));
        }
    }
}
