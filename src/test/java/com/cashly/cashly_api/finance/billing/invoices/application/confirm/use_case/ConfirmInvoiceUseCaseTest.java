package com.cashly.cashly_api.finance.billing.invoices.application.confirm.use_case;

import com.cashly.cashly_api.auth.user.domain.entity.User;
import com.cashly.cashly_api.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services.CashMovementService;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.billing.invoices.application.confirm.request.ConfirmInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.notifications.application.services.NotificationService;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceLineId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.entity.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.warehouses.stock.domain.repository.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.domain.repository.WarehouseStockRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmInvoiceUseCase Unit Tests")
class ConfirmInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private InvoiceIssueValidator authorizationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CashMovementService cashMovementService;

    private ConfirmInvoiceUseCase confirmInvoiceUseCase;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient().when(authorizationService.validate(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new InvoiceIssueValidator.CashPaymentPlan(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        var actors = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalActorIdentityGateway(
                new com.cashly.cashly_api.auth.user.application.queries.services.RepositoryActorIdentityQuery(userRepository,
                        mock(com.cashly.cashly_api.auth.user.domain.services.PasswordEncoder.class)));
        var inventory = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInventoryGatewayAdapter(
                new com.cashly.cashly_api.warehouses.stock.application.invoices.services.InvoiceInventoryService(
                        warehouseStockRepository, stockMovementRepository));
        var notifications = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceNotificationGateway(notificationService);
        var cash = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalInvoiceCashRegisterGateway(
                new com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.services.CashRegisterOperationsService(cashMovementService));
        confirmInvoiceUseCase = new ConfirmInvoiceUseCase(invoiceRepository, new IssueInvoiceService(invoiceRepository,
                actors, authorizationService, mock(InvoicePaymentService.class), inventory,
                mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway.class), notifications, cash));
    }

    @Test
    void should_ConfirmInvoice_When_InvoiceIsDraftAndCash() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH);
        User user = buildUser(authenticatedUserId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, false, null, null);
        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id, request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("PAID", response.status());
        verify(invoiceRepository, atLeastOnce()).save(any(Invoice.class));
        verify(notificationService).notifyCustomerInvoiceConfirmed(any(), any(), any());
        verify(cashMovementService).record(
                eq(CashMovementDirection.IN), eq(CashMovementType.INVOICE),
                any(BigDecimal.class), isNull(), contains("INV-"),
                eq("INVOICE"), eq(id), eq(authenticatedUserId), eq("testuser"));
    }

    @Test
    void should_ConfirmInvoice_When_InvoiceIsDraftAndCredit() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CREDIT);
        User user = buildUser(authenticatedUserId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, false, null, null);
        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id, request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.status());
        verify(invoiceRepository, atLeastOnce()).save(any(Invoice.class));
        verify(notificationService).notifyCustomerInvoiceConfirmed(any(), any(), any());
        verify(cashMovementService).record(
                eq(CashMovementDirection.IN), eq(CashMovementType.INVOICE),
                any(BigDecimal.class), isNull(), contains("INV-"),
                eq("INVOICE"), eq(id), eq(authenticatedUserId), eq("testuser"));
    }

    @Test
    void should_ThrowException_When_InvoiceNotFound() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.empty());

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, false, null, null);
        assertThrows(InvoiceNotFoundException.class, () -> 
            confirmInvoiceUseCase.execute(id, request, authenticatedUserId));
    }

    @Test
    void should_CreateStockMovement_When_InvoiceConfirmed() {
        UUID id = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithLine(id, InvoiceStatus.DRAFT, PaymentType.CASH, warehouseId, productId);
        WarehouseStock stock = WarehouseStock.create(warehouseId, productId);
        stock.addStock(100);
        stock.reserve(10);
        User user = buildUser(authenticatedUserId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(warehouseStockRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            return new StockMovement(UUID.randomUUID(), m.getWarehouseId(), m.getProductId(),
                    m.getMovementType(), m.getQuantity(), m.getPreviousStock(), m.getNewStock(),
                    m.getOriginMovementId(), m.getTransferBatchId(), m.getReferenceType(),
                    m.getReferenceId(), m.getReferenceNumber(), m.getNotes(), m.getCreatedAt());
        });

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, false, null, null);
        confirmInvoiceUseCase.execute(id, request, authenticatedUserId);

        verify(stockMovementRepository).save(argThat(movement -> 
            movement.getMovementType() == MovementType.SALE_EXIT &&
            movement.getQuantity() == 10
        ));
    }

    @Test
    void should_reject_confirmation_and_stop_side_effects_when_allocation_stock_is_missing() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoiceWithLine(id, InvoiceStatus.DRAFT, PaymentType.CASH,
                UUID.randomUUID(), UUID.randomUUID());
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(userRepository.findById(new UserId(authenticatedUserId)))
                .thenReturn(Optional.of(buildUser(authenticatedUserId)));
        assertThrows(com.cashly.cashly_api.warehouses.stock.application.exceptions.ProductNotInWarehouseException.class,
                () -> confirmInvoiceUseCase.execute(id, new ConfirmInvoiceRequest(null, false, null, null), authenticatedUserId));
        verify(invoiceRepository, never()).save(any());
        verify(warehouseStockRepository, never()).save(any());
        verifyNoInteractions(stockMovementRepository, cashMovementService, notificationService);
    }
    @Test
    void should_stop_before_authorization_and_effects_when_actor_is_missing() {
        UUID id = UUID.randomUUID();
        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CASH)));
        var error = assertThrows(IllegalArgumentException.class, () -> confirmInvoiceUseCase.execute(id,
                new ConfirmInvoiceRequest(null, false, null, null), authenticatedUserId));
        assertEquals("Usuario no encontrado", error.getMessage());
        verify(invoiceRepository, never()).save(any());
        verifyNoInteractions(authorizationService, warehouseStockRepository, stockMovementRepository, cashMovementService, notificationService);
    }
    private Invoice buildInvoice(UUID id, InvoiceStatus status, PaymentType paymentType) {
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2024, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                null,
                null,
                status,
                paymentType,
                null,
                BigDecimal.ZERO,
                null,
                null,
                null,
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Invoice buildInvoiceWithLine(UUID id, InvoiceStatus status, PaymentType paymentType, 
                                         UUID warehouseId, UUID productId) {
        InvoiceLineAllocation allocation = InvoiceLineAllocation.create(warehouseId, BigDecimal.TEN);
        InvoiceLine line = InvoiceLine.reconstitute(
                InvoiceLineId.of(UUID.randomUUID()),
                productId,
                "Test Product",
                "PROD-001",
                BigDecimal.TEN,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(100),
                List.of(allocation)
        );
        return Invoice.reconstitute(
                InvoiceId.of(id),
                InvoiceNumber.of(2024, 1),
                UUID.randomUUID(),
                "Test Customer",
                "J-12345678-9",
                UUID.randomUUID(),
                "Test User",
                null,
                null,
                status,
                paymentType,
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

    private User buildUser(UUID id) {
        return new User(
                new UserId(id),
                new Email("test@example.com"),
                new UserName("testuser"),
                Password.fromHash("hashedPassword"),
                UserRole.ADMIN
        );
    }
}
