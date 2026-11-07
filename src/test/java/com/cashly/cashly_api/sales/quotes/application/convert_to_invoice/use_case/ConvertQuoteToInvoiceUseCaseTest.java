package com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.use_case;

import com.cashly.cashly_api.platform.auth.user.domain.entity.User;
import com.cashly.cashly_api.platform.auth.user.domain.repository.UserRepository;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Email;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.Password;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserId;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserName;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.InvoicePaymentService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.validation.InvoiceIssueValidator;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.request.IssueInvoiceCommand;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.model.InvoiceOrigin;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.application.issue.services.IssueInvoiceService;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceStatus;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.response.QuoteConvertedToInvoiceResponse;
import com.cashly.cashly_api.sales.quotes.domain.entity.Quote;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteAllocation;
import com.cashly.cashly_api.sales.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.sales.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.sales.quotes.domain.repository.QuoteRepository;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteNumber;
import com.cashly.cashly_api.sales.quotes.domain.valueobjects.QuoteStatus;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConvertQuoteToInvoiceUseCase Unit Tests")
class ConvertQuoteToInvoiceUseCaseTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceIssueValidator authorizationService;


    @Mock
    private com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway creditTerms;

    private ConvertQuoteToInvoiceUseCase useCase;

    private final UUID quoteId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        lenient()
                .when(authorizationService.validate(any(), any(), any(), any(),
                        any(), any(), any()))
                .thenReturn(new InvoiceIssueValidator.CashPaymentPlan(
                        java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));
        lenient().when(creditTerms.creditDays(any())).thenReturn(0);
        var actors = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalActorIdentityGateway(
                new com.cashly.cashly_api.platform.auth.user.application.queries.services.RepositoryActorIdentityQuery(
                        userRepository,
                        mock(com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder.class)));
        var stock = new com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration.LocalStockDeductionGateway(
                new com.cashly.cashly_api.inventory.warehouses.stock.application.deduction.services.StockDeductionService(
                        warehouseStockRepository));
        var issuance = new IssueInvoiceService(invoiceRepository, actors,
                authorizationService, mock(InvoicePaymentService.class),
                mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway.class),
                stock,
                mock(com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CashRegisterGateway.class));
        useCase = new ConvertQuoteToInvoiceUseCase(quoteRepository,
                new com.cashly.cashly_api.sales.quotes.infrastructure.integration.LocalInvoiceCreationGateway(
                        new com.cashly.cashly_api.finance.billing.invoices.application.create.from_quote.services.QuoteInvoiceCreationService(
                                invoiceRepository, actors, creditTerms,
                                issuance)),
                org.mockito.Mockito.mock(com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder.class));
    }

    @Test
    @DisplayName("should convert to PAID invoice when paymentType is CASH")
    void shouldConvertToPaidInvoiceWhenCash() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Quote quote = buildDraftQuote(quoteId, warehouseId, productId);
        User user = buildUser(authenticatedUserId);
        WarehouseStock stock = new WarehouseStock(null, warehouseId, productId,
                50, 0, null, null);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId)))
                .thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(3);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository
                .findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest(
                "CASH", null, null, null, null);
        QuoteConvertedToInvoiceResponse result = useCase.execute(quoteId,
                request, authenticatedUserId);

        assertNotNull(result);
        assertEquals(quoteId, result.quoteId());
        assertNotNull(result.invoiceId());
        assertNotNull(result.invoiceNumber());

        assertEquals(48, stock.getQuantity());

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor
                .forClass(Invoice.class);
        verify(invoiceRepository, times(2)).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        assertEquals(InvoiceStatus.PAID, savedInvoice.getStatus());
        assertEquals(PaymentType.CASH, savedInvoice.getPaymentType());

        verify(quoteRepository).save(quote);
        assertEquals(result.invoiceId(), quote.getInvoiceId());
    }

    @Test
    @DisplayName("should convert to PENDING_PAYMENT invoice when paymentType is CREDIT")
    void shouldConvertToPendingPaymentWhenCredit() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Quote quote = buildDraftQuote(quoteId, warehouseId, productId);
        User user = buildUser(authenticatedUserId);
        WarehouseStock stock = new WarehouseStock(null, warehouseId, productId,
                50, 0, null, null);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId)))
                .thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(7);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository
                .findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest(
                "CREDIT", null, null, null, null);
        QuoteConvertedToInvoiceResponse result = useCase.execute(quoteId,
                request, authenticatedUserId);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor
                .forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        assertEquals(InvoiceStatus.PENDING_PAYMENT, savedInvoice.getStatus());
        assertEquals(PaymentType.CREDIT, savedInvoice.getPaymentType());
    }

    @Test
    @DisplayName("should throw when paymentType is invalid")
    void shouldThrowWhenPaymentTypeInvalid() {
        Quote quote = buildDraftQuote(quoteId, UUID.randomUUID(),
                UUID.randomUUID());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest(
                "BITCOIN", null, null, null, null);
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(quoteId, request, authenticatedUserId));

        verify(invoiceRepository, never()).save(any());
        verify(warehouseStockRepository, never())
                .findByWarehouseIdAndProductIdForUpdate(any(), any());
    }

    @Test
    @DisplayName("should throw when quote is not in DRAFT status")
    void shouldThrowWhenQuoteNotDraft() {
        QuoteLine line = new QuoteLine(UUID.randomUUID(), "Prod", "P-001",
                BigDecimal.ONE, BigDecimal.TEN);
        Quote quote = Quote.reconstitute(quoteId,
                new QuoteNumber("PRE-2026-0001"), UUID.randomUUID(), "Customer",
                "J-123", authenticatedUserId, "Creator", QuoteStatus.CANCELLED,
                null, null, BigDecimal.TEN, null, null, null, null, null, null,
                List.of(line), java.time.Instant.now(),
                java.time.Instant.now());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest(
                "CASH", null, null, null, null);
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(quoteId, request, authenticatedUserId));

        assertEquals("Only DRAFT quotes can be converted to invoices",
                exception.getMessage());
        verify(invoiceRepository, never()).save(any());
        verify(warehouseStockRepository, never())
                .findByWarehouseIdAndProductIdForUpdate(any(), any());
    }

    @Test
    @DisplayName("should throw when quote is not found")
    void shouldThrowWhenQuoteNotFound() {
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest(
                "CASH", null, null, null, null);
        assertThrows(QuoteNotFoundException.class,
                () -> useCase.execute(quoteId, request, authenticatedUserId));

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldPreserveExplicitIssueDateAndCustomerCreditDays() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Quote quote = buildDraftQuote(quoteId, warehouseId, productId);
        LocalDate issueDate = LocalDate.of(2026, 9, 1);
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId)))
                .thenReturn(Optional.of(buildUser(authenticatedUserId)));
        when(creditTerms.creditDays(quote.getCustomerId())).thenReturn(15);
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(7);
        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseStockRepository
                .findByWarehouseIdAndProductIdForUpdate(warehouseId, productId))
                .thenReturn(Optional.of(new WarehouseStock(null, warehouseId,
                        productId, 50, 0, null, null)));

        useCase.execute(quoteId, new ConvertQuoteToInvoiceRequest("CREDIT",
                null, null, null, null, issueDate), authenticatedUserId);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        assertEquals(issueDate, captor.getValue().getIssueDate());
        assertEquals(issueDate.plusDays(15), captor.getValue().getDueDate());
        verify(invoiceRepository)
                .getNextSequenceForYear(LocalDate.now().getYear());
    }

    @Test
    void shouldLeaveStockAndQuoteUnchangedWhenAuthorizationFails() {
        Quote quote = buildDraftQuote(quoteId, UUID.randomUUID(),
                UUID.randomUUID());
        var failure = new IllegalArgumentException("Authorization rejected");
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId)))
                .thenReturn(Optional.of(buildUser(authenticatedUserId)));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(7);
        doThrow(failure).when(authorizationService).validate(any(), any(),
                any(), any(), any(), any(), any());

        assertSame(failure,
                assertThrows(IllegalArgumentException.class,
                        () -> useCase.execute(quoteId,
                                new ConvertQuoteToInvoiceRequest("CREDIT",
                                        "password", null, null, null),
                                authenticatedUserId)));

        verifyNoInteractions(warehouseStockRepository);
        verify(invoiceRepository, never()).save(any());
        verify(quoteRepository, never()).save(any());
        assertEquals(QuoteStatus.DRAFT, quote.getStatus());
    }

    private Quote buildDraftQuote(UUID quoteId, UUID warehouseId,
            UUID productId) {
        QuoteLine line = new QuoteLine(productId, "Product A", "PROD-001",
                BigDecimal.valueOf(2), new BigDecimal("50.0000"));
        QuoteAllocation allocation = new QuoteAllocation(productId, warehouseId,
                "Main WH", BigDecimal.valueOf(2));
        line.addAllocation(allocation);

        return Quote.reconstitute(quoteId, new QuoteNumber("PRE-2026-0002"),
                UUID.randomUUID(), "Empresa Test C.A.", "J-12345678-9",
                authenticatedUserId, "Creator", QuoteStatus.DRAFT,
                LocalDate.of(2026, 6, 30), null, new BigDecimal("100.0000"),
                null, null, null, null, null, null, List.of(line),
                java.time.Instant.now(), java.time.Instant.now());
    }

    private User buildUser(UUID userId) {
        return new User(new UserId(userId), new Email("user@cashly.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password"));
    }
}
