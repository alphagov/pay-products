package uk.gov.pay.products.fixtures;

import uk.gov.pay.products.persistence.entity.ChargeEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

public class ChargeEntityFixture {

    private String externalId;
    private ProductEntity productEntity;
    private Long price;

    public ChargeEntityFixture() {
    }

    public ChargeEntity build() {
        ChargeEntity chargeEntity = new ChargeEntity();
        chargeEntity.setExternalId(externalId);
        chargeEntity.setPrice(price);
        chargeEntity.setProductEntity(productEntity);

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

    public ChargeEntityFixture withProduct(ProductEntity productEntity) {
        this.productEntity = productEntity;
        return this;
    }
}
