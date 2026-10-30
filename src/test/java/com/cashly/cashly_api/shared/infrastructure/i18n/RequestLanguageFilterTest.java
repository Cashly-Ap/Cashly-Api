package com.cashly.cashly_api.shared.infrastructure.i18n;

import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestLanguageFilterTest {
    private final UserPreferencesRepository preferences = mock(UserPreferencesRepository.class);
    private final RequestLanguageFilter filter = new RequestLanguageFilter(preferences);
    @AfterEach void reset() {
        LocaleContextHolder.resetLocaleContext();
        SecurityContextHolder.clearContext();
    }
    @Test void authenticatedPreferenceWinsOverRequestHeader() throws Exception {
        var user = UUID.randomUUID();
        when(preferences.languageFor(user)).thenReturn("en");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.toString(), "", List.of()));
        var request = new MockHttpServletRequest(); request.addHeader("Accept-Language", "es");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            assertThat(MessageTranslator.currentLocale().getLanguage()).isEqualTo("en");
            assertThat(req.getAttribute("cashly.locale")).isEqualTo(Locale.ENGLISH);
        });
        assertThat(response.getHeader("Content-Language")).isEqualTo("en");
        assertThat(LocaleContextHolder.getLocaleContext()).isNull();
    }
    @Test void unauthenticatedRequestUsesSupportedHeaderAndRestoresOriginalContext() throws Exception {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        var request = new MockHttpServletRequest(); request.addHeader("Accept-Language", "fr,en-GB;q=0.8");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) ->
            assertThat(MessageTranslator.currentLocale()).isEqualTo(Locale.ENGLISH));
        assertThat(MessageTranslator.currentLocale().getLanguage()).isEqualTo("es");
    }
    @Test void exceptionsDoNotLeakAnotherUsersLocaleToNextRequest() {
        var request = new MockHttpServletRequest(); request.addHeader("Accept-Language", "en");
        assertThatThrownBy(() -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            throw new jakarta.servlet.ServletException("test failure");
        })).isInstanceOf(jakarta.servlet.ServletException.class);
        assertThat(LocaleContextHolder.getLocaleContext()).isNull();
        assertThat(MessageTranslator.currentLocale().getLanguage()).isEqualTo("es");
    }
}
