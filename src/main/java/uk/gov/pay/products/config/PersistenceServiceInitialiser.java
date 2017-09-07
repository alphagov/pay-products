package uk.gov.pay.products.config;

import com.google.inject.persist.PersistService;

public class PersistenceServiceInitialiser {

    @javax.inject.Inject
    public PersistenceServiceInitialiser(PersistService service) {
        service.start();
    }
}
