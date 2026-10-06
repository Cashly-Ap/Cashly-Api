package com.cashly.cashly_api.quotes.domain.valueobjects;

import java.util.regex.Pattern;

public record QuoteNumber(String value) {

    private static final String PREFIX = "PRE";
    private static final Pattern FORMAT = Pattern.compile("^PRE-\\d{4}-\\d{4}$");
    private static final String FORMAT_STRING = "PRE-%d-%04d";

    public QuoteNumber {
        if (value == null) {
            throw new IllegalArgumentException("QuoteNumber cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("QuoteNumber cannot be blank");
        }
        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "QuoteNumber must match format PRE-YYYY-NNNN, got: " + value);
        }
    }

    public static QuoteNumber of(int year, int sequence) {
        if (sequence <= 0) {
            throw new IllegalArgumentException("Sequence must be greater than zero");
        }
        if (sequence > 9999) {
            throw new IllegalArgumentException("Sequence must be at most 9999");
        }
        return new QuoteNumber(String.format(FORMAT_STRING, year, sequence));
    }
}
