package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.invoices.application.services.InvoiceAuthorizationService;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.quotes.application.dto.request.ConvertQuoteToInvoiceRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToInvoiceResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
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
    private InvoiceAuthorizationService authorizationService;

    private ConvertQuoteToInvoiceUseCase useCase;

    private final UUID quoteId = UUID.randomUUID();
    private final UUID authenticatedUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConvertQuoteToInvoiceUseCase(
                quoteRepository, invoiceRepository, warehouseStockRepository, userRepository, authorizationService);
    }

    @Test
    @DisplayName("should convert to PAID invoice when paymentType is CASH")
    void shouldConvertToPaidInvoiceWhenCash() {
        UUID warehouseId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Quote quote = buildDraftQuote(quoteId, warehouseId, productId);
        User user = buildUser(authenticatedUserId);
        WarehouseStock stock = new WarehouseStock(null, warehouseId, productId, 50, 0, null, null);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(3);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest("CASH", null, null, null, null);
        QuoteConvertedToInvoiceResponse result = useCase.execute(quoteId, request, authenticatedUserId);

        assertNotNull(result);
        assertEquals(quoteId, result.quoteId());
        assertNotNull(result.invoiceId());
        assertNotNull(result.invoiceNumber());

        
        assertEquals(48, stock.getQuantity());

        
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
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
        WarehouseStock stock = new WarehouseStock(null, warehouseId, productId, 50, 0, null, null);

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));
        when(userRepository.findById(new UserId(authenticatedUserId))).thenReturn(Optional.of(user));
        when(invoiceRepository.getNextSequenceForYear(anyInt())).thenReturn(7);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(warehouseStockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
                .thenReturn(Optional.of(stock));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest("CREDIT", null, null, null, null);
        QuoteConvertedToInvoiceResponse result = useCase.execute(quoteId, request, authenticatedUserId);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        Invoice savedInvoice = invoiceCaptor.getValue();
        assertEquals(InvoiceStatus.PENDING_PAYMENT, savedInvoice.getStatus());
        assertEquals(PaymentType.CREDIT, savedInvoice.getPaymentType());
    }

    @Test
    @DisplayName("should throw when paymentType is invalid")
    void shouldThrowWhenPaymentTypeInvalid() {
        Quote quote = buildDraftQuote(quoteId, UUID.randomUUID(), UUID.randomUUID());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest("BITCOIN", null, null, null, null);
        assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(quoteId, request, authenticatedUserId));

        verify(invoiceRepository, never()).save(any());
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
    }

    @Test
    @DisplayName("should throw when quote is not in DRAFT status")
    void shouldThrowWhenQuoteNotDraft() {
        QuoteLine line = new QuoteLine(UUID.randomUUID(), "Prod", "P-001", BigDecimal.ONE, BigDecimal.TEN);
        Quote quote = Quote.reconstitute(quoteId, new QuoteNumber("PRE-2026-0001"),
                UUID.randomUUID(), "Customer", "J-123",
                authenticatedUserId, "Creator", QuoteStatus.CANCELLED,
                null, null, BigDecimal.TEN, null, null, null, null,
                null, null, List.of(line), java.time.Instant.now(), java.time.Instant.now());

        when(quoteRepository.findById(quoteId)).thenReturn(Optional.of(quote));

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest("CASH", null, null, null, null);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                useCase.execute(quoteId, request, authenticatedUserId));

        assertEquals("Only DRAFT quotes can be converted to invoices", exception.getMessage());
        verify(invoiceRepository, never()).save(any());
        verify(warehouseStockRepository, never()).findByWarehouseIdAndProductId(any(), any());
    }

    @Test
    @DisplayName("should throw when quote is not found")
    void shouldThrowWhenQuoteNotFound() {
        when(quoteRepository.findById(quoteId)).thenReturn(Optional.empty());

        ConvertQuoteToInvoiceRequest request = new ConvertQuoteToInvoiceRequest("CASH", null, null, null, null);
        assertThrows(QuoteNotFoundException.class, () ->
                useCase.execute(quoteId, request, authenticatedUserId));

        verify(invoiceRepository, never()).save(any());
    }

    

    private Quote buildDraftQuote(UUID quoteId, UUID warehouseId, UUID productId) {
        QuoteLine line = new QuoteLine(productId, "Product A", "PROD-001", BigDecimal.valueOf(2), new BigDecimal("50.0000"));
        QuoteAllocation allocation = new QuoteAllocation(productId, warehouseId, "Main WH", BigDecimal.valueOf(2));
        line.addAllocation(allocation);

        return Quote.reconstitute(quoteId, new QuoteNumber("PRE-2026-0002"),
                UUID.randomUUID(), "Empresa Test C.A.", "J-12345678-9",
                authenticatedUserId, "Creator", QuoteStatus.DRAFT,
                LocalDate.of(2026, 6, 30), null, new BigDecimal("100.0000"),
                null, null, null, null,
                null, null,
                List.of(line), java.time.Instant.now(), java.time.Instant.now());
    }

    private User buildUser(UUID userId) {
        return new User(
                new UserId(userId),
                new Email("user@parsun.com"),
                new UserName("Test User"),
                Password.fromHash("hashed-password")
        );
    }
}
