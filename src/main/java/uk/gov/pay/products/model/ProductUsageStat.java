package uk.gov.pay.products.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.products.persistence.entity.ProductEntity;
import uk.gov.service.payments.commons.api.json.ApiResponseDateTimeSerializer;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductUsageStat {
    
    @JsonProperty
    @Schema(example = "120")
    private final Long paymentCount;
    @JsonProperty
    @JsonSerialize(using = ApiResponseDateTimeSerializer.class)
    @Schema(example = "\"2022-04-04T19:17:39.790Z\"")
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
