package uk.gov.pay.products.util;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Errors {

    @JsonProperty("errors")
    private List<String> errors;
    @JsonProperty("error_identifier")
    private String errorIdentifier;

    private Errors(List<String> errors) {
        this.errors = errors;
        this.errorIdentifier = null;
    }

    private Errors(List<String> errors, String errorIdentifier) {
        this.errorIdentifier = errorIdentifier;
        this.errors = errors;
    }

    public static Errors from(String error) {
        return new Errors(Collections.singletonList(error));
    }

    public static Errors from(List<String> errorList) {
        return new Errors(errorList);
    }

    public static Errors from(String error, String errorIdentifier) { 
        return new Errors(Collections.singletonList(error), errorIdentifier);
    }

    @JsonGetter
    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
