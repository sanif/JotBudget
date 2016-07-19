package com.bestapps.jotbudget.util;


public final class CurrencyUtil {
    private static final String DECIMAL_FORMAT = "%02d";

    private CurrencyUtil() {

    }

    public static String sanitize(String val) {
        return toUnsignedCurrencyString(fromString(val));
    }

    /**
     * Interpret string as long value, striping everything but digits before parsing it.
     */
    public static long fromString(String val) {
        String clean = val.replaceAll("\\D+", "");
        return Long.parseLong(clean);
    }
    
    public static String toSignedCurrencyString(long val) {
        if (val == 0) {
            return toString(val, false); // don't add sign to '0'
        }
        return toString(val, true);
    }

    public static String toUnsignedCurrencyString(long val) {
        if (val < 0) {
            throw new IllegalArgumentException("val < 0");
        }
        return toString(val, false);
    }

    private static String toString(long val, boolean addSign) {
        long valUnsigned = Math.abs(val);

        long decimal = valUnsigned % 100;
        long integral = valUnsigned / 100;

        final String unsigned = integral + "." + String.format(DECIMAL_FORMAT, decimal);
        final String signed;
        if (addSign) {
            final String sign;
            if (val < 0) {
                sign = "-";
            } else {
                sign = "+";
            }
            signed = sign + unsigned;
        } else {
            signed = unsigned;
        }
        return signed;
    }
}
