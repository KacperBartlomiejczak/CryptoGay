package com.example.cryptogay.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final DecimalFormat STANDARD_FORMAT;
    private static final DecimalFormat SMALL_PRICE_FORMAT;
    private static final DecimalFormat PERCENTAGE_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        STANDARD_FORMAT = new DecimalFormat("$#,##0.00", symbols);
        SMALL_PRICE_FORMAT = new DecimalFormat("$#,##0.0000", symbols);
        PERCENTAGE_FORMAT = new DecimalFormat("0.00", symbols);
    }

    private CurrencyFormatter() {
        // Utility class
    }

    public static String formatPrice(Double price) {
        if (price == null) {
            return "$0.00";
        }
        if (price >= 1.0 || price == 0.0) {
            return STANDARD_FORMAT.format(price);
        } else {
            return SMALL_PRICE_FORMAT.format(price);
        }
    }

    public static String formatPercentage(Double percentage) {
        if (percentage == null) {
            return "0.00%";
        }
        String prefix = percentage > 0 ? "+" : "";
        return prefix + PERCENTAGE_FORMAT.format(percentage) + "%";
    }

    public static boolean isPositive(Double percentage) {
        return percentage != null && percentage >= 0.0;
    }

    public static String formatLargeCurrency(Double amount) {
        if (amount == null || amount == 0.0) {
            return "$0.00";
        }
        if (amount >= 1_000_000_000_000.0) {
            return String.format(Locale.US, "$%.2f T", amount / 1_000_000_000_000.0);
        } else if (amount >= 1_000_000_000.0) {
            return String.format(Locale.US, "$%.2f B", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000.0) {
            return String.format(Locale.US, "$%.2f M", amount / 1_000_000.0);
        } else {
            return formatPrice(amount);
        }
    }

    public static String formatSupply(Double supply, String symbol) {
        if (supply == null || supply == 0.0) {
            return "N/A";
        }
        String sym = symbol != null ? " " + symbol.toUpperCase() : "";
        if (supply >= 1_000_000_000_000.0) {
            return String.format(Locale.US, "%.2f T%s", supply / 1_000_000_000_000.0, sym);
        } else if (supply >= 1_000_000_000.0) {
            return String.format(Locale.US, "%.2f B%s", supply / 1_000_000_000.0, sym);
        } else if (supply >= 1_000_000.0) {
            return String.format(Locale.US, "%.2f M%s", supply / 1_000_000.0, sym);
        } else {
            return String.format(Locale.US, "%,.0f%s", supply, sym);
        }
    }
}

