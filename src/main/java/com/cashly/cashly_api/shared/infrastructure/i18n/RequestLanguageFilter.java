package com.cashly.cashly_api.shared.infrastructure.i18n;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RequestLanguageFilter extends OncePerRequestFilter {
    private final UserPreferencesRepository preferences;
    public RequestLanguageFilter(UserPreferencesRepository preferences) { this.preferences = preferences; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var previous = LocaleContextHolder.getLocaleContext();
        var language = requestedLanguage(request.getHeader("Accept-Language"));
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                var id = UUID.fromString(authentication.getName());
                String saved = preferences.languageFor(id);
                if ("es".equals(saved) || "en".equals(saved)) language = saved;
            } catch (IllegalArgumentException ignored) { }
        }
        var locale = Locale.forLanguageTag(language);
        LocaleContextHolder.setLocale(locale);
        request.setAttribute("cashly.locale", locale);
        response.setHeader("Content-Language", language);
        try { chain.doFilter(request, response); }
        finally { LocaleContextHolder.setLocaleContext(previous); }
    }
    public static String requestedLanguage(String header) {
        if (header == null || header.isBlank()) return "es";
        try {
            for (var range : Locale.LanguageRange.parse(header)) {
                if (range.getWeight() == 0) continue;
                var language = Locale.forLanguageTag(range.getRange()).getLanguage();
                if (language.equals("en") || language.equals("es")) return language;
            }
        } catch (IllegalArgumentException ignored) { }
        return "es";
    }
}
