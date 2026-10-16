package com.cashly.cashly_api.auth.user.application.queries;
import com.cashly.cashly_api.auth.user.domain.User;
import com.cashly.cashly_api.auth.user.domain.UserRepository;
import com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class RepositoryActorIdentityQueryTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final RepositoryActorIdentityQuery query = new RepositoryActorIdentityQuery(users, passwords);
    private final UUID id = UUID.randomUUID();
    @Test
    void should_expose_identity_and_match_password_inside_auth_without_exporting_hash() {
        var user = new User(new UserId(id), new Email("operator@example.com"), new UserName("Operator"), Password.fromHash("stored-hash"), UserRole.ADMIN);
        when(users.findById(new UserId(id))).thenReturn(Optional.of(user));
        when(passwords.matches("provided-password", "stored-hash")).thenReturn(true);
        var actor = query.findById(id).orElseThrow();
        assertThat(actor.id()).isEqualTo(id);
        assertThat(actor.name()).isEqualTo("Operator");
        assertThat(actor.getClass().getRecordComponents()).extracting(component -> component.getName()).containsExactly("id", "name");
        assertThat(query.matchesPassword(id, "provided-password")).isTrue();
        assertThat(query.matchesPassword(id, "wrong-password")).isFalse();
        verify(passwords).matches("provided-password", "stored-hash");
    }
    @Test
    void should_preserve_missing_actor_result_and_password_lookup_error() {
        assertThat(query.findById(id)).isEmpty();
        assertThatThrownBy(() -> query.matchesPassword(id, "password")).isInstanceOf(IllegalArgumentException.class).hasMessage("Usuario no encontrado");
        verifyNoInteractions(passwords);
    }
}
