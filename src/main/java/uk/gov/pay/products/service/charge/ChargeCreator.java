package uk.gov.pay.products.service.charge;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import uk.gov.pay.products.model.Charge;
import uk.gov.pay.products.persistence.dao.ChargeDao;
import uk.gov.pay.products.persistence.dao.ProductDao;
import uk.gov.pay.products.persistence.entity.ChargeEntity;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import java.util.Optional;

import static uk.gov.pay.products.util.RandomIdGenerator.randomUuid;

public class ChargeCreator {

    private ChargeDao chargeDao;
    private ProductDao productDao;

    @Inject
    public ChargeCreator(ChargeDao chargeDao, ProductDao productDao) {
        this.chargeDao = chargeDao;
        this.productDao = productDao;
    }

    @Transactional
    public Optional<Charge> doCreate(Charge charge) {

        Optional<ProductEntity> productMaybe = productDao.findByExternalId(charge.getProductExternalId());

        if (!productMaybe.isPresent()) {
            return Optional.empty();
        }

        ProductEntity productEntity = productMaybe.get();
        ChargeEntity chargeEntity = ChargeEntity.from(charge);
        chargeEntity.setExternalId(randomUuid());
        chargeEntity.setProductExternalId(productEntity.getExternalId());
        chargeEntity.setDescription(productEntity.getDescription());
        chargeEntity.setPrice(charge.getPrice() == null ? productEntity.getPrice() : charge.getPrice());
        chargeDao.persist(chargeEntity);

        return Optional.of(chargeEntity.toCharge());
    }
}
