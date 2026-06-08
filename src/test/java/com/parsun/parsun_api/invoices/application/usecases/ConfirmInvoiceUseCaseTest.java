package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.services.PasswordEncoder;
import com.parsun.parsun_api.auth.user.domain.valueobjects.*;
import com.parsun.parsun_api.customers.application.usecases.ConsumeCustomerBalanceUseCase;
import com.parsun.parsun_api.invoices.application.dto.request.ConfirmInvoiceRequest;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.*;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.payments.domain.repositories.PaymentRepository;
import com.parsun.parsun_api.shared.services.CreditGuard;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    private CreditGuard creditGuard;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ConsumeCustomerBalanceUseCase consumeCustomerBalanceUseCase;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAllocationRepository allocationRepository;

    private ConfirmInvoiceUseCase confirmInvoiceUseCase;

    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        confirmInvoiceUseCase = new ConfirmInvoiceUseCase(invoiceRepository,
                warehouseStockRepository, stockMovementRepository, creditGuard,
                userRepository, passwordEncoder, consumeCustomerBalanceUseCase,
                paymentRepository, allocationRepository);
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
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void should_ConfirmInvoice_When_InvoiceIsDraftAndCredit() {
        UUID id = UUID.randomUUID();
        Invoice invoice = buildInvoice(id, InvoiceStatus.DRAFT, PaymentType.CREDIT);
        User user = buildUser(authenticatedUserId);

        when(invoiceRepository.findById(InvoiceId.of(id))).thenReturn(Optional.of(invoice));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(creditGuard.isCreditAvailable(any(), any())).thenReturn(true);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfirmInvoiceRequest request = new ConfirmInvoiceRequest(null, false, null, null);
        InvoiceDetailResponse response = confirmInvoiceUseCase.execute(id, request, authenticatedUserId);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.status());
        verify(invoiceRepository).save(any(Invoice.class));
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
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
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
