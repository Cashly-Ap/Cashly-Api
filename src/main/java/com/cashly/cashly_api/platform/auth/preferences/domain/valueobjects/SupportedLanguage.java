package com.cashly.cashly_api.platform.auth.preferences.domain.valueobjects;

public enum SupportedLanguage {
    SPANISH("es"), ENGLISH("en");
    private final String code;
    SupportedLanguage(String code) {
        this.code = code;
    }
    public String code() {
        return code;
    }
    public static String require(String code) {
        for (var language : values())
            if (language.code.equals(code))
                return code;
        throw new IllegalArgumentException("Unsupported language");
    }
}
