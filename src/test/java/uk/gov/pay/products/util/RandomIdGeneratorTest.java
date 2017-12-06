package uk.gov.pay.products.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RandomIdGeneratorTest {

    @Test
    public void shouldGenerateARandomUserFriendlyReference() throws Exception {
        String reference = RandomIdGenerator.randomUserFriendlyReference();
        assertThat(reference.replace("-", "").length(), is(10));

        //will always contains a number (or more)
        assertThat(reference.matches(".*\\d.*"),is(true));
    }

}
