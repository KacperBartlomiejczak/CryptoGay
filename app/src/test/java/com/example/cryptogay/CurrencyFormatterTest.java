package com.example.cryptogay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.cryptogay.util.CurrencyFormatter;

import org.junit.Test;

public class CurrencyFormatterTest {

    @Test
    public void formatPrice_normalPrices_formatsWithTwoDecimals() {
        assertEquals("$64,250.00", CurrencyFormatter.formatPrice(64250.0));
        assertEquals("$1.50", CurrencyFormatter.formatPrice(1.5));
        assertEquals("$0.00", CurrencyFormatter.formatPrice(0.0));
    }

    @Test
    public void formatPrice_smallPrices_formatsWithFourDecimals() {
        assertEquals("$0.0045", CurrencyFormatter.formatPrice(0.0045));
    }

    @Test
    public void formatPrice_null_returnsZeroDollars() {
        assertEquals("$0.00", CurrencyFormatter.formatPrice(null));
    }

    @Test
    public void formatPercentage_positive_addsPlusPrefix() {
        assertEquals("+2.45%", CurrencyFormatter.formatPercentage(2.45));
    }

    @Test
    public void formatPercentage_negative_formatsMinus() {
        assertEquals("-1.20%", CurrencyFormatter.formatPercentage(-1.20));
    }

    @Test
    public void formatPercentage_null_returnsZero() {
        assertEquals("0.00%", CurrencyFormatter.formatPercentage(null));
    }

    @Test
    public void isPositive_correctlyIdentifiesSign() {
        assertTrue(CurrencyFormatter.isPositive(0.01));
        assertTrue(CurrencyFormatter.isPositive(0.0));
        assertFalse(CurrencyFormatter.isPositive(-0.01));
        assertFalse(CurrencyFormatter.isPositive(null));
    }

    @Test
    public void formatLargeCurrency_formatsTrillionsBillionsMillions() {
        assertEquals("$1.59 T", CurrencyFormatter.formatLargeCurrency(1_590_000_000_000.0));
        assertEquals("$31.69 B", CurrencyFormatter.formatLargeCurrency(31_690_000_000.0));
        assertEquals("$20.08 M", CurrencyFormatter.formatLargeCurrency(20_080_000.0));
        assertEquals("$500.00", CurrencyFormatter.formatLargeCurrency(500.0));
        assertEquals("$0.00", CurrencyFormatter.formatLargeCurrency(null));
    }

    @Test
    public void formatSupply_formatsWithSymbol() {
        assertEquals("19.80 M BTC", CurrencyFormatter.formatSupply(19_800_000.0, "BTC"));
        assertEquals("N/A", CurrencyFormatter.formatSupply(null, "BTC"));
    }
}
