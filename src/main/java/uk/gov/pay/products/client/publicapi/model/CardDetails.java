package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CardDetails {

    private final String lastDigitsCardNumber;
    private final String firstDigitsCardNumber;
    private final String cardHolderName;
    private final String expiryDate;
    private final Address billingAddress;
    private final CardBrand cardBrand;

    public CardDetails(
            @JsonProperty("last_digits_card_number") String lastDigitsCardNumber,
            @JsonProperty("first_digits_card_number") String firstDigitsCardNumber,
            @JsonProperty("cardholder_name") String cardHolderName,
            @JsonProperty("expiry_date") String expiryDate,
            @JsonProperty("billing_address") Address billingAddress,
            @JsonProperty("card_brand") CardBrand cardBrand) {
        this.lastDigitsCardNumber = lastDigitsCardNumber;
        this.firstDigitsCardNumber = firstDigitsCardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.billingAddress = billingAddress;
        this.cardBrand = cardBrand;
    }

    public String getLastDigitsCardNumber() {
        return lastDigitsCardNumber;
    }
    
    public String getFirstDigitsCardNumber() {
        return firstDigitsCardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public Address getBillingAddress() {
        return billingAddress;
    }

    public CardBrand getCardBrand() {
        return cardBrand;
    }

    @Override
    public String toString() {
        return "CardDetails{" +
                "lastDigitsCardNumber='" + lastDigitsCardNumber + '\'' +
                ", firstDigitsCardNumber='" + firstDigitsCardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", billingAddress=" + billingAddress +
                ", cardBrand='" + cardBrand + '\'' +
                '}';
    }
}
