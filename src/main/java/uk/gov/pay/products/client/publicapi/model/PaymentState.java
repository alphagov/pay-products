package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentState {
    private String status;
    private boolean finished;
    private String message;
    private String code;

    public PaymentState(
            @JsonProperty("status") String status,
            @JsonProperty("finished") boolean finished,
            @JsonProperty("message") String message,
            @JsonProperty("code") String code) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "PaymentState{" +
                "status='" + status + '\'' +
                ", finished=" + finished +
                ", message='" + message + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
