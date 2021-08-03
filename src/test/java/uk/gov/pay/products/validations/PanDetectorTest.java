package uk.gov.pay.products.validations;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PanDetectorTest {
    @Test
    public void shouldDetectPotentialPAN() {
        assertTrue(PanDetector.isSuspectedPan("4242424242424242"));
    }

    @Test
    public void shouldDetectPotentialPAN_whenDigitsAreSeparatedByHyphens() {
        assertTrue(PanDetector.isSuspectedPan("42424242-42424242"));
    }
    
    @Test
    public void shouldDetectPotentialPAN_whenDigitsAreSeparatedBySpaces() {
        assertTrue(PanDetector.isSuspectedPan("424242424 2424242"));
    }

    @Test
    public void shouldNotDetectPotentialPAN_whenNumberIsShorterThan12Digits() {
        assertFalse(PanDetector.isSuspectedPan("42424242424"));
    }

    @Test
    public void shouldNotDetectPotentialPAN_whenNumberIsLongerThan19Digits() {
        assertFalse(PanDetector.isSuspectedPan("42424242424242424242"));
    }
    
    @Test
    public void shouldNotDetectPotentialPAN_whenNumberDoesNotPassLuhnAlgorithm() {
        assertFalse(PanDetector.isSuspectedPan("1111111111111111"));
    }
}
