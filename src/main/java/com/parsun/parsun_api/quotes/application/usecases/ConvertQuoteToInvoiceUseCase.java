package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceStatus;
import com.parsun.parsun_api.invoices.domain.valueobjects.PaymentType;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteConvertedToInvoiceResponse;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ConvertQuoteToInvoiceUseCase {

    private final QuoteRepository quoteRepository;
    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final UserRepository userRepository;

    public ConvertQuoteToInvoiceUseCase(QuoteRepository quoteRepository,
                                         InvoiceRepository invoiceRepository,
                                         WarehouseStockRepository warehouseStockRepository,
                                         UserRepository userRepository) {
        this.quoteRepository = quoteRepository;
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuoteConvertedToInvoiceResponse execute(UUID quoteId, String paymentType, UUID authenticatedUserId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        if (quote.getStatus() != com.parsun.parsun_api.quotes.domain.valueobjects.QuoteStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT quotes can be converted to invoices");
        }

        PaymentType resolvedPaymentType;
        try {
            resolvedPaymentType = PaymentType.valueOf(paymentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment type: " + paymentType + ". Must be CASH or CREDIT");
        }

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        int year = LocalDate.now().getYear();
        int sequence = invoiceRepository.getNextSequenceForYear(year);
        InvoiceNumber invoiceNumber = InvoiceNumber.of(year, sequence);

        InvoiceStatus status = resolvedPaymentType == PaymentType.CASH
                ? InvoiceStatus.PAID
                : InvoiceStatus.PENDING_PAYMENT;

        List<InvoiceLine> invoiceLines = quote.getLines().stream()
                .map(this::toInvoiceLine)
                .toList();

        Invoice invoice = Invoice.reconstitute(
                InvoiceId.generate(), invoiceNumber,
                quote.getCustomerId(), quote.getCustomerName(), quote.getCustomerRif(),
                authenticatedUserId, user.getUserName().getValue(),
                null, null,  // orderId, orderNumber (no order when converting from quote)
                status, resolvedPaymentType, null,
                BigDecimal.ZERO, null,
                null, null,
                invoiceLines, LocalDateTime.now(), LocalDateTime.now()
        );

        // Deduct stock for each line allocation
        for (QuoteLine quoteLine : quote.getLines()) {
            for (QuoteAllocation allocation : quoteLine.getAllocations()) {
                WarehouseStock stock = warehouseStockRepository
                        .findByWarehouseIdAndProductId(allocation.getWarehouseId(), allocation.getProductId())
                        .orElseThrow(() -> new IllegalStateException(
                                "No stock found for product " + allocation.getProductId() +
                                " in warehouse " + allocation.getWarehouseId()));

                int amount = allocation.getAllocatedQuantity().intValue();
                stock.removeStock(amount);
                warehouseStockRepository.save(stock);
            }
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        quote.markConvertedToInvoice(savedInvoice.getId().value(), savedInvoice.getInvoiceNumber().value());
        quoteRepository.save(quote);

        return new QuoteConvertedToInvoiceResponse(
                quote.getId(),
                quote.getQuoteNumber().value(),
                savedInvoice.getId().value(),
                savedInvoice.getInvoiceNumber().value()
        );
    }

    private InvoiceLine toInvoiceLine(QuoteLine quoteLine) {
        List<InvoiceLineAllocation> allocations = quoteLine.getAllocations().stream()
                .map(a -> InvoiceLineAllocation.create(a.getWarehouseId(), a.getAllocatedQuantity()))
                .toList();

        return InvoiceLine.create(
                quoteLine.getProductId(),
                quoteLine.getProductName(),
                quoteLine.getProductCode(),
                quoteLine.getQuantity(),
                quoteLine.getUnitPrice(),
                allocations
        );
    }
}
