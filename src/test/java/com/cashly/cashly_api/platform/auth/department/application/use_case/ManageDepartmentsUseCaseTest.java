package com.cashly.cashly_api.platform.auth.department.application.use_case;

import com.cashly.cashly_api.platform.auth.authorization.application.exceptions.AccessConflictException;
import com.cashly.cashly_api.platform.auth.authorization.application.ports.out.AccessRepository;
import com.cashly.cashly_api.platform.auth.authorization.application.services.AccessService;
import com.cashly.cashly_api.platform.auth.department.domain.entity.Department;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManageDepartmentsUseCaseTest {
    private final AccessRepository repository = mock(AccessRepository.class);
    private final AccessService access = mock(AccessService.class);
    private final ManageDepartmentsUseCase useCase = new ManageDepartmentsUseCase(
            repository, access, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    private final UUID id = UUID.randomUUID();

    @Test
    void rejects_case_insensitive_duplicates_and_referenced_deactivation_or_deletion() {
        var department = new Department(id, "Operations", null, true, 0);
        when(repository.department(id)).thenReturn(Optional.of(department));
        when(repository.departments()).thenReturn(List.of(department));
        when(repository.departmentReferenced(id)).thenReturn(true);
        assertThatThrownBy(
                () -> useCase.save(null,
                        new ManageDepartmentsUseCase.Request(" operations ",
                                null, true, null)))
                .isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(
                () -> useCase.save(id,
                        new ManageDepartmentsUseCase.Request("Operations", null,
                                false, 0L)))
                .isInstanceOf(AccessConflictException.class);
        assertThatThrownBy(() -> useCase.delete(id))
                .isInstanceOf(AccessConflictException.class);
        verify(repository, never()).saveDepartment(any(), anyBoolean());
        verify(repository, never()).deleteDepartment(any());
    }
}
