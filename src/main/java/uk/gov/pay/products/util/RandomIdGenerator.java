package uk.gov.pay.products.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import static java.util.stream.IntStream.range;

public class RandomIdGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Random RANDOM = new Random();
    /**
     * source
     */
    private static final String RANDOM_SOURCE_SET_1 = "ABCSDEFGHIJKLMNPQRSTUVWXYZ";
    private static final String RANDOM_SOURCE_SET_2 = "123456789";
    private static final String RANDOM_SOURCE_UNION = RANDOM_SOURCE_SET_1.concat(RANDOM_SOURCE_SET_2);

    /**
     * This method will generate a URL safe random string.
     * This works by choosing 130 bits from a cryptographically secure random bit generator,
     * and encoding them in base-32.
     * <p> 128 bits is considered to be cryptographically strong,
     * but each digit in a base 32 number can encode 5 bits, so 128 is rounded up to the next multiple of 5.
     * This encoding is compact and efficient, with 5 random bits per character. Compare this to a random UUID,
     * which only has 3.4 bits per character in standard layout, and only 122 random bits in total </p>
     *
     * @return a random number in base32 (in string format)
     */
    public static String newId() {
        return new BigInteger(130, SECURE_RANDOM).toString(32);
    }

    public static Integer randomInt() {
        return RANDOM.nextInt(Integer.MAX_VALUE);
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    /**
     * Random string generator of length 10 (excluding `-`)
     * Uses sets of upper case alphabets and numbers (excluding `0` and `o`) with biased on digits on every 4th character.
     * <p>
     * probability set (34 ^ 8) * (9 ^ 2) = 1.4464931e+14 (in the range of 10 - 100^ trillion)
     *
     * @return a user friendly reference of the format XXX-XXXX-XXX
     */
    public static String randomUserFriendlyReference() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        range(0, 10)
                .forEach(i -> {
                    if (i % 4 == 2) {
                        sb.append(RANDOM_SOURCE_SET_2.charAt(random.nextInt(RANDOM_SOURCE_SET_2.length())));
                        sb.append("-");
                    } else {
                        sb.append(RANDOM_SOURCE_UNION.charAt(random.nextInt(RANDOM_SOURCE_UNION.length())));
                    }
                });
        return sb.toString();
    }
}
