package uk.gov.pay.products.client.publicapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SettlementSummary {

    private String captureSubmitTime;
    private String capturedDate;

    public SettlementSummary(
            @JsonProperty("capture_submit_time") String captureSubmitTime,
            @JsonProperty("captured_date") String capturedDate) {
        this.captureSubmitTime = captureSubmitTime;
        this.capturedDate = capturedDate;
    }

    public String getCaptureSubmitTime() {
        return captureSubmitTime;
    }

    public void setCaptureSubmitTime(String captureSubmitTime) {
        this.captureSubmitTime = captureSubmitTime;
    }

    public String getCapturedDate() {
        return capturedDate;
    }

    public void setCapturedDate(String capturedDate) {
        this.capturedDate = capturedDate;
    }

    @Override
    public String toString() {
        return "SettlementSummary{" +
                "captureSubmitTime='" + captureSubmitTime + '\'' +
                ", capturedDate='" + capturedDate + '\'' +
                '}';
    }
}
