package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CardBrand {

    @JsonProperty("Visa")
    VISA,

    @JsonProperty("Mastercard")
    MASTER_CARD,

    @JsonProperty("American Express")
    AMERICAN_EXPRESS,

    @JsonProperty("Diners Club")
    DINERS_CLUB,

    @JsonProperty("Discover")
    DISCOVER,

    @JsonProperty("Jcb")
    JCB,

    @JsonProperty("Union Pay")
    UNIONPAY
}
