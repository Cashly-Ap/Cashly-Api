package com.cashly.cashly_api.shared.infrastructure.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class I18nConfig {
    @Bean public MessageSource messageSource() { return MessageTranslator.messageSource(); }
    @Bean public org.springframework.web.servlet.LocaleResolver localeResolver() {
        return new org.springframework.web.servlet.i18n.AbstractLocaleResolver() {
            @Override public java.util.Locale resolveLocale(jakarta.servlet.http.HttpServletRequest request) {
                Object locale = request.getAttribute("cashly.locale");
                return locale instanceof java.util.Locale value ? value
                        : java.util.Locale.forLanguageTag(RequestLanguageFilter.requestedLanguage(request.getHeader("Accept-Language")));
            }
            @Override public void setLocale(jakarta.servlet.http.HttpServletRequest request,
                    jakarta.servlet.http.HttpServletResponse response, java.util.Locale locale) {
                request.setAttribute("cashly.locale", MessageTranslator.supported(locale));
            }
        };
    }
    @Bean public MessageTranslator messageTranslator() { return new MessageTranslator(); }
    @Bean public LocalValidatorFactoryBean defaultValidator() {
        var validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource());
        return validator;
    }
}
