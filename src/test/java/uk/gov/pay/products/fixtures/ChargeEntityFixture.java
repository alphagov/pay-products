package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.ChargeEntity;

public class ChargeEntityFixture {

    private String externalId;
    private String productExternalId;
    private Long price;

    public ChargeEntityFixture() {
    }

    public ChargeEntity build() {
        ChargeEntity chargeEntity = new ChargeEntity();
        chargeEntity.setExternalId(externalId);
        chargeEntity.setPrice(price);
        chargeEntity.setProductExternalId(productExternalId);

        return chargeEntity;
    }

    public static ChargeEntityFixture aChargeEntity() {
        return new ChargeEntityFixture();
    }

    public ChargeEntityFixture withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public ChargeEntityFixture withPrice(Long price) {
        this.price = price;
        return this;
    }

    public ChargeEntityFixture withProductExternalId(String productExternalId) {
        this.productExternalId = productExternalId;
        return this;
    }
}
