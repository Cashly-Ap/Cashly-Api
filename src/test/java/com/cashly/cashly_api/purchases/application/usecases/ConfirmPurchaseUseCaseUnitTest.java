package com.cashly.cashly_api.purchases.application.usecases;

import com.cashly.cashly_api.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.purchases.domain.entities.Purchase;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLine;
import com.cashly.cashly_api.purchases.domain.entities.PurchaseLineAllocation;
import com.cashly.cashly_api.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseLineId;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.warehouses.domain.entities.MovementType;
import com.cashly.cashly_api.warehouses.domain.entities.ReferenceType;
import com.cashly.cashly_api.warehouses.domain.entities.StockMovement;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.StockMovementRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;
import com.cashly.cashly_api.purchases.infrastructure.integration.WarehouseInventoryAdapter;
import com.cashly.cashly_api.warehouses.application.services.PurchaseInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmPurchaseUseCase Unit Tests")
class ConfirmPurchaseUseCaseUnitTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    private ConfirmPurchaseUseCase confirmPurchaseUseCase;

    @BeforeEach
    void setUp() {
        confirmPurchaseUseCase = new ConfirmPurchaseUseCase(purchaseRepository,
                new WarehouseInventoryAdapter(new PurchaseInventoryService(warehouseStockRepository, stockMovementRepository)), new com.cashly.cashly_api.purchases.application.services.PurchaseDetailResponseAssembler(new com.cashly.cashly_api.purchases.infrastructure.integration.LocalPurchaseWarehouseGatewayAdapter(new com.cashly.cashly_api.warehouses.application.queries.RepositoryPurchaseWarehouseQuery(warehouseRepository, mock(WarehouseStockRepository.class)))));
    }

    @Test
    void should_ConfirmPurchase_When_PurchaseIsDraft() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = confirmPurchaseUseCase.execute(id);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void should_ThrowException_When_PurchaseNotFound() {
        UUID id = UUID.randomUUID();
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class, () -> confirmPurchaseUseCase.execute(id));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_ConfirmingNonDraftPurchase() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.CANCELLED);
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));

        assertThrows(IllegalStateException.class, () -> confirmPurchaseUseCase.execute(id));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_AddStockAndCreateMovement_When_PurchaseConfirmed() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.DRAFT, warehouseId, productId);

        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(stock);
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(warehouseStockRepository).findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository).save(argThat(savedStock ->
                savedStock.getQuantity() == 10));
        verify(stockMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.ENTRY &&
                movement.getReferenceType() == ReferenceType.PURCHASE &&
                movement.getQuantity() == 10 &&
                movement.getProductId().equals(productId) &&
                movement.getWarehouseId().equals(warehouseId) &&
                movement.getPreviousStock() == 0 && movement.getNewStock() == 10 &&
                movement.getReferenceId().equals(id) &&
                movement.getReferenceNumber().equals(purchase.getPurchaseNumber().getValue()) &&
                movement.getNotes().equals("Compra confirmada")
        ));
    }

    @Test
    void should_CreateNewWarehouseStock_When_NoExistingStock() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id, PurchaseStatus.DRAFT, warehouseId, productId);

        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(warehouseStockRepository.findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(WarehouseStock.create(warehouseId, productId));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(warehouseStockRepository).findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId, productId);
        verify(warehouseStockRepository).save(argThat(savedStock ->
                savedStock.getQuantity() == 10));
        verify(stockMovementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.ENTRY &&
                movement.getReferenceType() == ReferenceType.PURCHASE
        ));
    }

    @Test
    void should_SavePurchaseExactlyOnce_When_ConfirmationSucceeds() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        when(purchaseRepository.findById(PurchaseId.of(id))).thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    private Purchase buildPurchase(UUID id, PurchaseStatus status) {
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A", "PROD-001",
                BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Purchase.reconstitute(
                PurchaseId.of(id),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                status,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Purchase buildPurchaseWithAllocation(UUID id, PurchaseStatus status,
                                                  UUID warehouseId, UUID productId) {
        PurchaseLineAllocation allocation = PurchaseLineAllocation.create(warehouseId, BigDecimal.TEN);
        PurchaseLine line = PurchaseLine.reconstitute(
                PurchaseLineId.of(UUID.randomUUID()),
                productId,
                "Test Product",
                "PROD-001",
                BigDecimal.TEN,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                List.of(allocation)
        );
        return Purchase.reconstitute(
                PurchaseId.of(id),
                PurchaseNumber.of(2026, 1),
                UUID.randomUUID(),
                "Supplier Name",
                "J-12345678-9",
                UUID.randomUUID(),
                "User Name",
                status,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(line),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
