package com.cashly.cashly_api.partners.suppliers.domain.entity;

import com.cashly.cashly_api.partners.suppliers.application.delete.use_case.DeleteSupplierUseCase;
import com.cashly.cashly_api.partners.suppliers.application.queries.use_case.ListSuppliersUseCase;

import com.cashly.cashly_api.partners.suppliers.application.ports.out.PurchaseUsageGateway;
import com.cashly.cashly_api.partners.suppliers.application.exceptions.SupplierHasAssociatedRecordsException;
import com.cashly.cashly_api.partners.suppliers.application.exceptions.SupplierNotFoundException;
import com.cashly.cashly_api.partners.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.partners.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierPurchaseUsageTest {
    private final SupplierRepository suppliers = mock(SupplierRepository.class);
    private final PurchaseUsageGateway usage = mock(PurchaseUsageGateway.class);
    private final UUID id = UUID.randomUUID();

    @Test
    void shouldRejectForeignModuleDependenciesInApplicationConstructors() {
        for (Class<?> type : List.of(DeleteSupplierUseCase.class,
                ListSuppliersUseCase.class)) {
            for (var constructor : type.getConstructors()) {
                for (Class<?> dependency : constructor.getParameterTypes()) {
                    assertFalse(dependency.getPackageName().startsWith(
                            "com.cashly.cashly_api.procurement.purchases"),
                            dependency.getName());
                }
            }
        }
    }

    @Test
    void shouldDeleteSupplierWithoutPurchases() {
        when(suppliers.existsById(SupplierId.from(id.toString())))
                .thenReturn(true);
        new DeleteSupplierUseCase(suppliers, usage, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class)).execute(id.toString());
        verify(usage).hasPurchases(id);
        verify(suppliers).delete(SupplierId.from(id.toString()));
    }

    @Test
    void shouldRejectDeletionWhenPurchasesExist() {
        when(suppliers.existsById(SupplierId.from(id.toString())))
                .thenReturn(true);
        when(usage.hasPurchases(id)).thenReturn(true);
        assertThrows(SupplierHasAssociatedRecordsException.class,
                () -> new DeleteSupplierUseCase(suppliers, usage, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class))
                        .execute(id.toString()));
        verify(suppliers, never()).delete(any());
    }

    @Test
    void shouldRejectMissingSupplierWithoutQueryingPurchases() {
        assertThrows(SupplierNotFoundException.class,
                () -> new DeleteSupplierUseCase(suppliers, usage, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class))
                        .execute(id.toString()));
        verifyNoInteractions(usage);
        verify(suppliers, never()).delete(any());
    }

    @Test
    void shouldPreservePurchaseFlagAndPaginationInListing() {
        UUID otherId = UUID.randomUUID();
        Supplier first = mock(Supplier.class);
        Supplier second = mock(Supplier.class);
        when(first.getId()).thenReturn(SupplierId.from(id.toString()));
        when(second.getId()).thenReturn(SupplierId.from(otherId.toString()));
        when(usage.findSupplierIdsWithPurchases(Set.of(id, otherId)))
                .thenReturn(Set.of(id));
        SupplierMapper mapper = mock(SupplierMapper.class);
        when(suppliers.findAllWithFilters(null, null, null, null, 1, 2, "code",
                "asc"))
                .thenReturn(new PageResponse<>(List.of(first, second), 5, 3, 2,
                        1, false, false, false));
        var result = new ListSuppliersUseCase(suppliers, mapper, usage)
                .execute(null, null, null, null, 1, 2, "code", "asc");
        verify(mapper).toListResponse(first, true);
        verify(mapper).toListResponse(second, false);
        verify(usage).findSupplierIdsWithPurchases(Set.of(id, otherId));
        verify(usage, never()).hasPurchases(any(UUID.class));
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals(1, result.number());
        assertEquals(2, result.size());
        assertFalse(result.first());
        assertFalse(result.last());
    }

    @Test
    void shouldNotDeleteWhenUsageLookupFails() {
        when(suppliers.existsById(SupplierId.from(id.toString())))
                .thenReturn(true);
        IllegalStateException failure = new IllegalStateException(
                "Purchase lookup failed");
        when(usage.hasPurchases(id)).thenThrow(failure);
        assertSame(failure,
                assertThrows(IllegalStateException.class,
                        () -> new DeleteSupplierUseCase(suppliers, usage, org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class))
                                .execute(id.toString())));
        verify(suppliers, never()).delete(any());
    }
    @Test
    void shouldAvoidPurchaseQueriesForEmptyListing() {
        when(suppliers.findAllWithFilters(null, null, null, null, 0, 10, "code",
                "asc"))
                .thenReturn(new PageResponse<>(List.of(), 0, 0, 10, 0, true,
                        true, true));
        var result = new ListSuppliersUseCase(suppliers,
                mock(SupplierMapper.class), usage)
                .execute(null, null, null, null, 0, 10, "code", "asc");
        assertTrue(result.empty());
        verifyNoInteractions(usage);
    }

}
