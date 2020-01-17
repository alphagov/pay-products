package uk.gov.pay.products.client.publicapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.commons.model.Source;

public class Internal {

    @JsonProperty("source")
    private Source source;

    public Internal(Source source) {
        this.source = source;
    }

    public Source getSource() {
        return source;
    }
}
