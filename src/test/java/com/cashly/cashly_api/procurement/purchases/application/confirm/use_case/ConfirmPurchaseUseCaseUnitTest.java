package com.cashly.cashly_api.procurement.purchases.application.confirm.use_case;

import com.cashly.cashly_api.procurement.purchases.application.dto.response.PurchaseDetailResponse;
import com.cashly.cashly_api.procurement.purchases.domain.entity.Purchase;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLine;
import com.cashly.cashly_api.procurement.purchases.domain.entity.PurchaseLineAllocation;
import com.cashly.cashly_api.procurement.purchases.domain.exceptions.PurchaseNotFoundException;
import com.cashly.cashly_api.procurement.purchases.domain.repository.PurchaseRepository;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseLineId;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseNumber;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PurchaseStatus;
import com.cashly.cashly_api.procurement.purchases.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.procurement.purchases.application.ports.out.PurchaseCashRegisterGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.inventory.warehouses.management.domain.repository.WarehouseRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import com.cashly.cashly_api.procurement.purchases.infrastructure.integration.WarehouseInventoryAdapter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.purchases.services.PurchaseInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Mock
    private PurchaseCashRegisterGateway purchaseCashRegisterGateway;

    private ConfirmPurchaseUseCase confirmPurchaseUseCase;

    @BeforeEach
    void setUp() {
        confirmPurchaseUseCase = new ConfirmPurchaseUseCase(purchaseRepository,
                new WarehouseInventoryAdapter(new PurchaseInventoryService(
                        warehouseStockRepository, stockMovementRepository)),
                new com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler(
                        new com.cashly.cashly_api.procurement.purchases.infrastructure.integration.LocalPurchaseWarehouseGatewayAdapter(
                                new com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services.RepositoryPurchaseWarehouseQuery(
                                        warehouseRepository,
                                        mock(WarehouseStockRepository.class)))), org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    }

    @Test
    void should_ConfirmPurchase_When_PurchaseIsDraft() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PurchaseDetailResponse response = confirmPurchaseUseCase.execute(id);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.status());
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void should_ThrowException_When_PurchaseNotFound() {
        UUID id = UUID.randomUUID();
        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.empty());

        assertThrows(PurchaseNotFoundException.class,
                () -> confirmPurchaseUseCase.execute(id));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_ThrowException_When_ConfirmingNonDraftPurchase() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.CANCELLED);
        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));

        assertThrows(IllegalStateException.class,
                () -> confirmPurchaseUseCase.execute(id));
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void should_AddStockAndCreateMovement_When_PurchaseConfirmed() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id,
                PurchaseStatus.DRAFT, warehouseId, productId);

        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);

        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));
        when(warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                        productId))
                .thenReturn(stock);
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(warehouseStockRepository)
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                        productId);
        verify(warehouseStockRepository)
                .save(argThat(savedStock -> savedStock.getQuantity() == 10));
        verify(stockMovementRepository).save(argThat(
                movement -> movement.getMovementType() == MovementType.ENTRY
                        && movement.getReferenceType() == ReferenceType.PURCHASE
                        && movement.getQuantity() == 10
                        && movement.getProductId().equals(productId)
                        && movement.getWarehouseId().equals(warehouseId)
                        && movement.getPreviousStock() == 0
                        && movement.getNewStock() == 10
                        && movement.getReferenceId().equals(id)
                        && movement.getReferenceNumber()
                                .equals(purchase.getPurchaseNumber().getValue())
                        && movement.getNotes().equals("Compra confirmada")));
    }

    @Test
    void should_CreateNewWarehouseStock_When_NoExistingStock() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Purchase purchase = buildPurchaseWithAllocation(id,
                PurchaseStatus.DRAFT, warehouseId, productId);

        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));
        when(warehouseStockRepository
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                        productId))
                .thenReturn(WarehouseStock.create(warehouseId, productId));
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(warehouseStockRepository)
                .findOrCreateByWarehouseIdAndProductIdForUpdate(warehouseId,
                        productId);
        verify(warehouseStockRepository)
                .save(argThat(savedStock -> savedStock.getQuantity() == 10));
        verify(stockMovementRepository).save(argThat(movement -> movement
                .getMovementType() == MovementType.ENTRY
                && movement.getReferenceType() == ReferenceType.PURCHASE));
    }

    @Test
    void should_RecordCashExpense_When_CashPurchaseIsConfirmed() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildCashPurchase(id, PurchaseStatus.DRAFT);
        ConfirmPurchaseUseCase useCase = new ConfirmPurchaseUseCase(
                purchaseRepository,
                new WarehouseInventoryAdapter(new PurchaseInventoryService(
                        warehouseStockRepository, stockMovementRepository)),
                new com.cashly.cashly_api.procurement.purchases.application.services.PurchaseDetailResponseAssembler(
                        new com.cashly.cashly_api.procurement.purchases.infrastructure.integration.LocalPurchaseWarehouseGatewayAdapter(
                                new com.cashly.cashly_api.inventory.warehouses.stock.application.queries.services.RepositoryPurchaseWarehouseQuery(
                                        warehouseRepository,
                                        mock(WarehouseStockRepository.class)))),
                mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class),
                null, null, purchaseCashRegisterGateway);
        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(id);

        verify(purchaseCashRegisterGateway).recordCashPurchase(
                eq(BigDecimal.TEN), eq(id),
                eq(purchase.getPurchaseNumber().getValue()),
                eq(purchase.getCreatedBy()), eq(purchase.getCreatedByName()));
    }

    @Test
    void should_SavePurchaseExactlyOnce_When_ConfirmationSucceeds() {
        UUID id = UUID.randomUUID();
        Purchase purchase = buildPurchase(id, PurchaseStatus.DRAFT);
        when(purchaseRepository.findById(PurchaseId.of(id)))
                .thenReturn(Optional.of(purchase));
        when(purchaseRepository.save(any(Purchase.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        confirmPurchaseUseCase.execute(id);

        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    private Purchase buildPurchase(UUID id, PurchaseStatus status) {
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A",
                "PROD-001", BigDecimal.ONE, BigDecimal.TEN, List.of());
        return Purchase.reconstitute(PurchaseId.of(id),
                PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier Name",
                "J-12345678-9", UUID.randomUUID(), "User Name", status, null,
                BigDecimal.ZERO, null, null, null, List.of(line),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Purchase buildCashPurchase(UUID id, PurchaseStatus status) {
        PurchaseLine line = PurchaseLine.create(UUID.randomUUID(), "Product A",
                "PROD-001", BigDecimal.ONE, BigDecimal.TEN, List.of());
        LocalDate issueDate = LocalDate.now();
        return Purchase.reconstitute(PurchaseId.of(id),
                PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier Name",
                "J-12345678-9", UUID.randomUUID(), "User Name", status,
                PaymentType.CASH, issueDate, issueDate.plusDays(30),
                BigDecimal.ZERO, null, null, BigDecimal.TEN, null, null, null,
                List.of(line), LocalDateTime.now(), LocalDateTime.now());
    }

    private Purchase buildPurchaseWithAllocation(UUID id, PurchaseStatus status,
            UUID warehouseId, UUID productId) {
        PurchaseLineAllocation allocation = PurchaseLineAllocation
                .create(warehouseId, BigDecimal.TEN);
        PurchaseLine line = PurchaseLine.reconstitute(
                PurchaseLineId.of(UUID.randomUUID()), productId, "Test Product",
                "PROD-001", BigDecimal.TEN, BigDecimal.valueOf(10),
                BigDecimal.valueOf(100), List.of(allocation));
        return Purchase.reconstitute(PurchaseId.of(id),
                PurchaseNumber.of(2026, 1), UUID.randomUUID(), "Supplier Name",
                "J-12345678-9", UUID.randomUUID(), "User Name", status, null,
                BigDecimal.ZERO, null, null, null, List.of(line),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
