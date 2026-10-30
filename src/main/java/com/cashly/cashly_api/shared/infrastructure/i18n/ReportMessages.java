package com.cashly.cashly_api.shared.infrastructure.i18n;

import org.springframework.context.i18n.LocaleContextHolder;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class ReportMessages {
    private static final MessageTranslator TRANSLATOR = new MessageTranslator();
    private ReportMessages() { }
    public static String text(String key) { return TRANSLATOR.text(key); }
    public static String code(String value) {
        if (value == null) return "";
        String key = "shared.reports.codes." + value.replace(' ', '_').toUpperCase(Locale.ROOT);
        return MessageTranslator.messageSource().getMessage(key, null, value, locale());
    }
    public static Locale locale() { return MessageTranslator.currentLocale(); }
    public static DateTimeFormatter dateFormatter() {
        return DateTimeFormatter.ofPattern(locale().getLanguage().equals("en") ? "MM/dd/yyyy" : "dd/MM/yyyy", locale());
    }
    public static DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern(locale().getLanguage().equals("en") ? "MM/dd/yyyy HH:mm" : "dd/MM/yyyy HH:mm", locale());
    }
}
