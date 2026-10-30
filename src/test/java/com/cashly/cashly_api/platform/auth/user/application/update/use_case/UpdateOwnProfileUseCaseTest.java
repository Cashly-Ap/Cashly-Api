package com.cashly.cashly_api.platform.auth.user.application.update.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.user.application.update.request.UpdateOwnProfileRequest;
import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateOwnProfileUseCaseTest {
    @Test
    void personalUpdateLocksBeforeReadAndPreservesAssignmentsAccountAndCredentials() {
        var users = mock(UserRepository.class);
        var access = mock(AccessService.class);
        var user = new User(UserId.generate(), new Email("own@example.com"), new UserName("Old Name"),
                Password.fromHash("existing-password-hash"), UserRole.OPERATOR);
        var role = UUID.randomUUID();
        var department = UUID.randomUUID();
        user.updateProfile(new UserProfile("Old", "Name", null, null), role, department);
        user.setAssignmentNames("Role", "Department");
        user.grantPermission(BusinessModule.USERS);
        user.deactivate();
        user.verifyEmail();
        var password = user.getPassword();
        var created = user.getCreatedAt();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(users.save(user)).thenReturn(user);
        var request = new UpdateOwnProfileRequest(" New Name ", " New ", " Last ",
                new UserProfile.Address("Street", "City", "Region", "Country", "10000"), "+34 600 000 000");

        var response = new UpdateOwnProfileUseCase(users, access, mock(com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository.class)).execute(user.getId().getValue().toString(), request);

        var order = inOrder(access, users);
        order.verify(access).lockAdministration();
        order.verify(users).findById(user.getId());
        order.verify(users).save(user);
        verifyNoMoreInteractions(access);
        assertThat(response.userName()).isEqualTo("New Name");
        assertThat(response.firstName()).isEqualTo("New");
        assertThat(response.phone()).isEqualTo("+34600000000");
        assertThat(response.address()).isEqualTo(request.address());
        assertThat(response.roleId()).isEqualTo(role);
        assertThat(response.departmentId()).isEqualTo(department);
        assertThat(response.roleName()).isEqualTo("Role");
        assertThat(response.departmentName()).isEqualTo("Department");
        assertThat(response.role()).isEqualTo(UserRole.OPERATOR);
        assertThat(response.modulePermissions()).containsExactly(BusinessModule.USERS);
        assertThat(response.email()).isEqualTo("own@example.com");
        assertThat(response.active()).isFalse();
        assertThat(response.emailVerified()).isTrue();
        assertThat(response.createdAt()).isEqualTo(created);
        assertThat(user.getPassword()).isSameAs(password);
    }

    @Test
    void profileUpdateUsesReadCommittedTransactionForSharedAdministrationLock() throws Exception {
        var method = UpdateOwnProfileUseCase.class.getMethod("execute", String.class, UpdateOwnProfileRequest.class);
        var transaction = method.getAnnotation(Transactional.class);
        assertThat(transaction).isNotNull();
        assertThat(transaction.isolation()).isEqualTo(Isolation.READ_COMMITTED);
        assertThat(transaction.readOnly()).isFalse();
    }

    @Test
    void missingAuthenticatedUserCannotSaveAnotherUser() {
        var users = mock(UserRepository.class);
        var access = mock(AccessService.class);
        var id = UserId.generate();
        when(users.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new UpdateOwnProfileUseCase(users, access, mock(com.cashly.cashly_api.platform.auth.preferences.domain.UserPreferencesRepository.class)).execute(id.getValue().toString(),
                new UpdateOwnProfileRequest("Name", "First", "Last", null, null)))
                .isInstanceOf(com.cashly.cashly_api.platform.auth.user.application.exceptions.UserNotFoundException.class);
        verify(users, never()).save(any());
    }
}
