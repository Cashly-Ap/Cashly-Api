package com.cashly.cashly_api.platform.auth.preferences;

import com.cashly.cashly_api.platform.auth.preferences.application.UserPreferencesService;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferences;
import com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserPreferencesServiceTest {
    final UserPreferencesRepository repository = mock(UserPreferencesRepository.class);
    final CurrentActor actor = mock(CurrentActor.class);
    final UserPreferencesService service = new UserPreferencesService(repository, actor);
    final UUID own = UUID.randomUUID();

    @Test void returnsDefaultsWithoutCreatingRow() {
        when(actor.requireId()).thenReturn(own);
        when(repository.findByUserId(own)).thenReturn(Optional.empty());
        assertThat(service.current()).isEqualTo(UserPreferences.defaults());
        verify(repository).findByUserId(own);
        verifyNoMoreInteractions(repository);
    }
    @Test void savesOnlyAuthenticatedAccountAndReturnsSavedValue() {
        when(actor.requireId()).thenReturn(own);
        var preferences = new UserPreferences("dark", "informative", true, true);
        assertThat(service.update(preferences)).isEqualTo(preferences);
        verify(repository).save(own, preferences);
        verifyNoMoreInteractions(repository);
    }
    @Test void accountSwitchLoadsSeparatePreferences() {
        var other = UUID.randomUUID();
        when(actor.requireId()).thenReturn(own, other);
        when(repository.findByUserId(own)).thenReturn(Optional.of(new UserPreferences("dark", "minimal", true, true)));
        when(repository.findByUserId(other)).thenReturn(Optional.empty());
        assertThat(service.current().themeMode()).isEqualTo("dark");
        assertThat(service.current()).isEqualTo(UserPreferences.defaults());
    }
    @Test void persistsHiddenHintsWithoutChangingOtherPreferences() {
        when(actor.requireId()).thenReturn(own);
        var hidden = new UserPreferences("system", "minimal", false, false);
        assertThat(service.update(hidden).showShortcutHints()).isFalse();
        verify(repository).save(own, hidden);
    }
    @Test void defaultsShowHintsEvenThoughShortcutsAreOff() {
        assertThat(UserPreferences.defaults().showShortcutHints()).isTrue();
        assertThat(UserPreferences.defaults().shortcutsEnabled()).isFalse();
    }
    @Test void domainRejectsUnsupportedAndMissingValues() {
        assertThatIllegalArgumentException().isThrownBy(() -> new UserPreferences("unknown", "minimal", false, true));
        assertThatIllegalArgumentException().isThrownBy(() -> new UserPreferences("light", "rainbow", false, true));
        assertThatIllegalArgumentException().isThrownBy(() -> new UserPreferences(null, "minimal", false, true));
    }
}
