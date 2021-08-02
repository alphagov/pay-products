package uk.gov.pay.products.validations;

import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;

public class PanDetector {
    private static final int MIN_PAN_LENGTH = 12;
    private static final int MAX_PAN_LENGTH = 19;
    
    public static boolean isSuspectedPan(String reference) {
        if (reference.length() < MIN_PAN_LENGTH || reference.length() > MAX_PAN_LENGTH) {
            return false;
        }
        var cleaned = reference
                .replace("-", "")
                .replace(" ", "");

        return (new LuhnCheckDigit()).isValid(cleaned);
    }
}
