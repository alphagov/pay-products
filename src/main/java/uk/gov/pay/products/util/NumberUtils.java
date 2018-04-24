package uk.gov.pay.products.util;

public class NumberUtils{

    public static boolean equals(Integer x, Integer y){
        return x == null ? y == null : org.apache.commons.lang3.math.NumberUtils.compare(x, y) == 0;
    }

    public static boolean equals(Long x, Long y){
        return x == null ? y == null : org.apache.commons.lang3.math.NumberUtils.compare(x, y) == 0;
    }
}
