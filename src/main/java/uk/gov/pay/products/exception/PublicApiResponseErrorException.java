package uk.gov.pay.products.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;

public class PublicApiResponseErrorException extends RuntimeException {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicApiResponseErrorException.class);

    private PublicApiErrorResponse error;
    private int status;

    public PublicApiResponseErrorException(Response response) {
        super(response.toString());
        this.status = response.getStatus();
        this.error = readError(response);
        response.close();
    }

    public int getErrorStatus() {
        return status;
    }

    public String getDescription() {
        if (error != null) {
            return error.getDescription();
        }
        return null;
    }

    public String getCode() {
        if (error != null) {
            return error.getCode();
        }
        return null;
    }

    public boolean hasDescription() {
        return this.error != null && this.error.getDescription() != null;
    }

    public boolean hasCode() {
        return this.error != null && this.error.getCode() != null;
    }

    private PublicApiErrorResponse readError(Response response) {
        PublicApiErrorResponse publicApiError = null;
        try {
            publicApiError = response.readEntity(PublicApiErrorResponse.class);
        } catch (Exception exception) {
            LOGGER.error("Could not read error response from Public API", exception);
        }
        return publicApiError;
    }

    private String getResponseBody() {
        if (error != null) {
            return error.toString();
        }
        return null;
    }

    @Override
    public String getMessage() {
        String body = getResponseBody();
        if (body != null) {
            return super.getMessage() + " and body " + body;
        }
        return super.getMessage();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PublicApiErrorResponse {

        private String field;
        private String code;
        private String description;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "PublicApiErrorResponse{" +
                    "field='" + field + '\'' +
                    ", code='" + code + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
