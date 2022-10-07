package uk.gov.pay.products.service;


public interface PaymentFactory {
    PaymentCreator paymentCreator();
    
    PaymentUpdater paymentUpdater();

    PaymentFinder paymentFinder();
}
