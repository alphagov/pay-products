package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.service.payments.commons.api.json.ApiResponseDateTimeSerializer;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductUsageStat {
    
    @JsonProperty
    private final Long paymentCount;
    @JsonProperty
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    private final ZonedDateTime lastPaymentDate;
    @JsonProperty
    private Product product;

    public ProductUsageStat(Long paymentCount, ZonedDateTime lastPaymentDate, ProductEntity productEntity) {
        this.paymentCount = paymentCount;
        this.lastPaymentDate = lastPaymentDate;
        this.product = productEntity.toProduct();
    }

    public Long getPaymentCount() {
        return paymentCount;
    }

    public Product getProduct() {
        return product;
    }

    public ZonedDateTime getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "ProductUsageStat{" +
                "paymentCount=" + paymentCount +
                ", lastPaymentDate=" + lastPaymentDate +
                ", product=" + product +
                '}';
    }
}
