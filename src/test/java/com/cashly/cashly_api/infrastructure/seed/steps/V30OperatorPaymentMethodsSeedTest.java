package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.OPERATOR_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.role.domain.entity.Role;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class V30OperatorPaymentMethodsSeedTest {

    @Mock
    private AccessRepository access;

    @Test
    void addsPaymentMethodReadGrantsToTheExistingOperatorRole() {
        var permissions = new EnumMap<PermissionModule, java.util.Set<PermissionAction>>(
                PermissionModule.class);
        permissions.put(PermissionModule.CONTACTS_CUSTOMERS,
                EnumSet.of(PermissionAction.VIEW_LIST));
        var role = new Role(OPERATOR_ROLE, "Operator", "Daily sales operations",
                true, 3, "OPERATOR", permissions);
        when(access.role(OPERATOR_ROLE)).thenReturn(Optional.of(role));

        new V30OperatorPaymentMethodsSeed(access).seed();

        var saved = ArgumentCaptor.forClass(Role.class);
        verify(access).saveRole(saved.capture(), eq(false));
        assertThat(saved.getValue().permissions())
                .containsEntry(PermissionModule.CONTACTS_CUSTOMERS,
                        EnumSet.of(PermissionAction.VIEW_LIST))
                .containsEntry(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS,
                        EnumSet.of(PermissionAction.VIEW_LIST,
                                PermissionAction.VIEW_DETAIL));
    }

    @Test
    void leavesTheOperatorRoleUntouchedWhenTheGrantsAlreadyExist() {
        var permissions = new EnumMap<PermissionModule, java.util.Set<PermissionAction>>(
                PermissionModule.class);
        permissions.put(PermissionModule.FINANCE_TREASURY_PAYMENT_METHODS,
                EnumSet.of(PermissionAction.VIEW_LIST,
                        PermissionAction.VIEW_DETAIL));
        var role = new Role(OPERATOR_ROLE, "Operator", "Daily sales operations",
                true, 3, "OPERATOR", permissions);
        when(access.role(OPERATOR_ROLE)).thenReturn(Optional.of(role));

        new V30OperatorPaymentMethodsSeed(access).seed();

        verify(access, never()).saveRole(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void ignoresAnAbsentOperatorRole() {
        when(access.role(OPERATOR_ROLE)).thenReturn(Optional.empty());

        new V30OperatorPaymentMethodsSeed(access).seed();

        verify(access, never()).saveRole(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyBoolean());
    }
}
