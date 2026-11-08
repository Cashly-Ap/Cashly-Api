package com.cashly.cashly_api.finance.billing.invoices.application.duplicate.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceNumber;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCatalogGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCustomerGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class DuplicateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InventoryGateway inventoryGateway;
    private final InvoiceCatalogGateway productRepository;
    private final InvoiceCustomerGateway customerRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DuplicateInvoiceUseCase(InvoiceRepository invoiceRepository,
            InventoryGateway inventoryGateway,
            InvoiceCatalogGateway productRepository,
            InvoiceCustomerGateway customerRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.inventoryGateway = inventoryGateway;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    @Audited(module = "finance", entityType = "INVOICE", action = "DUPLICATE")
    public InvoiceDetailResponse execute(UUID id, UUID newCustomerId) {
        Invoice original = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        UUID customerId = original.getCustomerId();
        String customerName = original.getCustomerName();
        String customerRif = original.getCustomerRif();

        if (newCustomerId != null) {
            InvoiceCustomerGateway.Customer customer = customerRepository
                    .findCustomer(newCustomerId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Customer not found: " + newCustomerId));
            customerId = customer.id();
            customerName = customer.name();
            customerRif = customer.rif();
        }

        int year = LocalDate.now().getYear();
        int sequence = invoiceRepository.getNextSequenceForYear(year);
        InvoiceNumber newNumber = InvoiceNumber.of(year, sequence);

        List<InvoiceLine> copiedLines = original.getLines().stream()
                .map(line -> {
                    InvoiceCatalogGateway.Product product = productRepository
                            .findProduct(line.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product not found: "
                                            + line.getProductId()));

                    BigDecimal currentPrice = line.getUnitPrice();

                    List<InvoiceLineAllocation> copiedAllocations = applyStockAllocations(
                            line);

                    return InvoiceLine.create(line.getProductId(),
                            product.name(), product.code(), line.getQuantity(),
                            currentPrice, copiedAllocations);
                }).toList();

        Invoice duplicate = Invoice.create(newNumber, customerId, customerName,
                customerRif, original.getCreatedBy(),
                original.getCreatedByName(), null, null,
                original.getPaymentType(), original.getNotes(), copiedLines);

        Invoice saved = invoiceRepository.save(duplicate);
        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_INVOICES",
                "DUPLICATE", "INVOICE", saved.getId().value(),
                saved.getInvoiceNumber().value(), null));
        return InvoiceMapper.toDetailResponse(saved);
    }

    private List<InvoiceLineAllocation> applyStockAllocations(
            InvoiceLine line) {
        inventoryGateway.reserveAllocations(line.getAllocations().stream()
                .map(a -> new InventoryGateway.Allocation(a.getWarehouseId(),
                        line.getProductId(), a.getAllocated().intValue()))
                .toList());
        return line.getAllocations().stream().map(a -> InvoiceLineAllocation
                .create(a.getWarehouseId(), a.getAllocated())).toList();
    }
}
