package uk.gov.pay.apps.util;

import org.postgresql.ssl.WrappedFactory;

public class TrustingSSLSocketFactory extends WrappedFactory {
    public TrustingSSLSocketFactory() {
        this._factory = TrustStoreLoader.getSSLContext().getSocketFactory();
    }
}
