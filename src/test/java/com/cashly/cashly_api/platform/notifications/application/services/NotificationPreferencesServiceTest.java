package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.notifications.application.ports.out.PermissionAccessGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.NotificationPreferences;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencesServiceTest {

    @Mock
    private PermissionAccessGateway access;
    @Mock
    private NotificationPreferencesRepository repository;

    private NotificationPreferencesService service;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new NotificationPreferencesService(access, repository);
        lenient().when(access.moduleLabelKey(any()))
                .thenAnswer(invocation -> "label." + invocation.getArgument(0));
    }

    @Test
    void defaults_to_enabled_when_no_rows_are_persisted() {
        when(access.resolve(userId)).thenReturn(new PermissionAccessGateway.Access(
                false, Set.of("CATALOG_PRODUCTS_CREATE",
                        "CATALOG_PRODUCTS_VIEW_LIST")));
        when(repository.find(userId)).thenReturn(Optional.empty());

        var view = service.get(userId);

        assertThat(view.enabled()).isTrue();
        assertThat(view.modules()).singleElement().satisfies(module -> {
            assertThat(module.module()).isEqualTo("CATALOG_PRODUCTS");
            assertThat(module.enabled()).isTrue();
        });
    }

    @Test
    void system_administrators_receive_all_mutating_modules() {
        when(access.resolve(userId)).thenReturn(new PermissionAccessGateway.Access(
                true, Set.of()));
        when(access.notificationEligibleModules())
                .thenReturn(List.of("FINANCE_BILLING_INVOICES", "USERS"));
        when(repository.find(userId)).thenReturn(Optional.empty());

        var view = service.get(userId);

        assertThat(view.modules()).extracting(
                NotificationPreferencesService.ModulePreference::module)
                .containsExactly("FINANCE_BILLING_INVOICES", "USERS");
    }

    @Test
    void rejects_modules_the_user_cannot_manage() {
        when(access.resolve(userId)).thenReturn(new PermissionAccessGateway.Access(
                false, Set.of("CATALOG_PRODUCTS_CREATE")));
        assertThatThrownBy(() -> service.update(userId, true,
                Map.of(PermissionModule.USERS.name(), false)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any(), any());
    }

    @Test
    void persists_and_returns_the_resulting_state() {
        when(access.resolve(userId)).thenReturn(new PermissionAccessGateway.Access(
                false, Set.of("CATALOG_PRODUCTS_CREATE")));
        when(repository.find(userId)).thenReturn(Optional
                .of(new NotificationPreferences(true, Map.of())))
                .thenReturn(Optional.of(new NotificationPreferences(false,
                        Map.of("CATALOG_PRODUCTS", false))));

        var view = service.update(userId, false,
                Map.of(PermissionModule.CATALOG_PRODUCTS.name(), false));

        assertThat(view.enabled()).isFalse();
        assertThat(view.modules()).singleElement()
                .satisfies(module -> assertThat(module.enabled()).isFalse());
        verify(repository).save(eq(userId), any(NotificationPreferences.class));
    }
}
