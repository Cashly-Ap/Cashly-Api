package com.cashly.cashly_api.shared.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.IOException;
import java.util.Locale;
import java.util.TreeSet;

public final class MessageTranslator {
    private static final MessageSource SOURCE = loadSource();
    public String text(String key, Object... arguments) {
        return translate(key, currentLocale(), arguments);
    }
    public String translate(String key, Locale locale, Object... arguments) {
        return SOURCE.getMessage(key, arguments, supported(locale));
    }
    public static Locale currentLocale() {
        return LocaleContextHolder.getLocaleContext() == null ? Locale.forLanguageTag("es")
                : supported(LocaleContextHolder.getLocale());
    }
    public static Locale supported(Locale locale) {
        return Locale.forLanguageTag(locale != null && "en".equals(locale.getLanguage()) ? "en" : "es");
    }
    public static MessageSource messageSource() { return SOURCE; }
    private static MessageSource loadSource() {
        var basenames = new TreeSet<String>();
        try {
            for (var resource : new PathMatchingResourcePatternResolver().getResources("classpath*:modules/**/i18n/*_es.properties")) {
                String path = resource.getURL().toExternalForm();
                basenames.add("classpath:" + path.substring(path.indexOf("modules/")).replace("_es.properties", ""));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load translation resources", exception);
        }
        var source = new ReloadableResourceBundleMessageSource();
        source.setBasenames(basenames.toArray(String[]::new));
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        source.setDefaultLocale(Locale.forLanguageTag("es"));
        return source;
    }
}
