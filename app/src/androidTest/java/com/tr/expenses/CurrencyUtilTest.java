package com.tr.expenses;

import com.tr.bnotes.util.CurrencyUtil;

import junit.framework.TestCase;

public class CurrencyUtilTest extends TestCase {
    public void testCurrencyConverter() {
        assertEquals(9999, CurrencyUtil.fromString("$@#!$@99.99$#%"));
        assertEquals("99.99", CurrencyUtil.toUnsignedCurrencyString(9999));
    }
}
