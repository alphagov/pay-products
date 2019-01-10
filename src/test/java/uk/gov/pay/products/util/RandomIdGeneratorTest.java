package uk.gov.pay.products.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RandomIdGeneratorTest {

    @Test
    public void shouldGenerateARandomUserFriendlyReference() {
        String reference = RandomIdGenerator.randomUserFriendlyReference();
        assertThat(reference.length(), is(10));

        //will always contains two numbers (or more)
        assertThat(reference.matches("(.*\\d){2}(.*)"),is(true));
    }

}
