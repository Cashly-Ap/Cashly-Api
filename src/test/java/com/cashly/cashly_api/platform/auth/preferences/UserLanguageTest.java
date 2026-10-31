package com.cashly.cashly_api.platform.auth.preferences;

import com.cashly.cashly_api.platform.auth.preferences.domain.entity.UserPreferences;
import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
import com.cashly.cashly_api.platform.auth.preferences.application.services.UserPreferencesService;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.in.CurrentActor;
import com.cashly.cashly_api.platform.auth.user.application.register.request.RegisterUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateOwnProfileRequest;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateUserRequest;
import com.cashly.cashly_api.platform.auth.user.application.update.use_case.UpdateOwnProfileUseCase;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.*;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserLanguageTest {
    @Test
    void spanishIsDefaultForExistingAndNewAccounts() {
        assertThat(UserPreferences.defaults().language()).isEqualTo("es");
        var request = new RegisterUserRequest("new@example.com", "New User",
                "Password1!", "New", "User", null, null, UUID.randomUUID(),
                UUID.randomUUID());
        assertThat(request.language()).isEqualTo("es");
        var repository = mock(UserPreferencesRepository.class,
                CALLS_REAL_METHODS);
        assertThat(repository.languageFor(UUID.randomUUID())).isEqualTo("es");
    }

    @Test
    void rejectsUnsupportedLanguagesInEveryWriteContract() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> UserPreferences.defaults().withLanguage("fr"));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UpdateOwnProfileRequest("Own User", "Own",
                        "User", null, null, "EN"));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new UpdateUserRequest("User", "Own", "User",
                        null, null, UUID.randomUUID(), UUID.randomUUID(), true,
                        true, "fr"));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new RegisterUserRequest("new@example.com",
                        "New User", "Password1!", "New", "User", null, null,
                        UUID.randomUUID(), UUID.randomUUID(), "de"));
    }

    @Test
    void languageChangePreservesOtherPreferences() {
        var own = UUID.randomUUID();
        var repository = mock(UserPreferencesRepository.class,
                CALLS_REAL_METHODS);
        var previous = new UserPreferences("dark", "informative", true, false,
                "es");
        when(repository.findByUserId(own)).thenReturn(Optional.of(previous));
        repository.updateLanguage(own, "en");
        verify(repository).save(own,
                new UserPreferences("dark", "informative", true, false, "en"));
    }

    @Test
    void oldPreferencesClientsCannotResetSavedLanguage() {
        var own = UUID.randomUUID();
        var repository = mock(UserPreferencesRepository.class);
        var actor = mock(CurrentActor.class);
        when(actor.requireId()).thenReturn(own);
        when(repository.findByUserId(own)).thenReturn(
                Optional.of(UserPreferences.defaults().withLanguage("en")));
        var saved = new UserPreferencesService(repository, actor).update(
                new UserPreferences("dark", "minimal", true, false), null);
        assertThat(saved.language()).isEqualTo("en");
        verify(repository).save(own, saved);
    }

    @Test
    void ownProfileSavesLanguageInPreferencesWithoutChangingOtherProfileFields() {
        var users = mock(UserRepository.class);
        var access = mock(AccessService.class);
        var preferences = mock(UserPreferencesRepository.class,
                CALLS_REAL_METHODS);
        var user = new User(UserId.generate(), new Email("own@example.com"),
                new UserName("Own User"), Password.fromHash("hash"));
        var id = user.getId().getValue();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        when(preferences.findByUserId(id)).thenReturn(Optional
                .of(new UserPreferences("dark", "minimal", true, false, "es")));
        var result = new UpdateOwnProfileUseCase(users, access, preferences)
                .execute(id.toString(), new UpdateOwnProfileRequest("Own User",
                        "Own", "User", null, null, "en"));
        verify(preferences).save(id,
                new UserPreferences("dark", "minimal", true, false, "en"));
        verify(access).lockAdministration();
        assertThat(result.email()).isEqualTo("own@example.com");
    }
}
