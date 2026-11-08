package com.cashly.cashly_api.finance.billing.invoices.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.finance.billing.invoices.application.update.request.UpdateInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.finance.billing.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.Invoice;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLine;
import com.cashly.cashly_api.finance.billing.invoices.domain.entity.InvoiceLineAllocation;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.repository.InvoiceRepository;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.InvoiceId;
import com.cashly.cashly_api.finance.billing.invoices.domain.valueobjects.PaymentType;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InventoryGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.InvoiceCatalogGateway;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.CustomerCreditTermsGateway;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

@Service
public class UpdateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceCatalogGateway productRepository;
    private final InventoryGateway inventoryGateway;
    private final StockAvailabilityGateway stockEnrichmentService;
    private final CustomerCreditTermsGateway customerRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateInvoiceUseCase(InvoiceRepository invoiceRepository,
            InvoiceCatalogGateway productRepository,
            InventoryGateway inventoryGateway,
            StockAvailabilityGateway stockEnrichmentService,
            CustomerCreditTermsGateway customerRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
        this.inventoryGateway = inventoryGateway;
        this.stockEnrichmentService = stockEnrichmentService;
        this.customerRepository = customerRepository;
    }

    @Transactional
    @Audited(module = "finance", entityType = "INVOICE", action = "EDIT")
    public InvoiceDetailResponse execute(UUID id,
            UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        revertStockAllocations(invoice);

        List<InvoiceLine> newLines = request.lines().stream()
                .map(lineRequest -> {
                    InvoiceCatalogGateway.Product product = productRepository
                            .findProduct(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product not found: "
                                            + lineRequest.productId()));

                    List<InvoiceLineAllocation> allocations = applyStockAllocations(
                            lineRequest, product.id());

                    return InvoiceLine.create(product.id(), product.name(),
                            product.code(), lineRequest.quantity(),
                            lineRequest.unitPrice(), allocations);
                }).toList();

        PaymentType paymentType = PaymentType.valueOf(request.paymentType());
        invoice.update(paymentType, request.notes(), newLines);
        if (request.issueDate() != null) {
            int creditDays = customerRepository
                    .creditDays(invoice.getCustomerId());
            invoice.updateDates(request.issueDate(),
                    request.issueDate().plusDays(creditDays));
        }

        Invoice saved = invoiceRepository.save(invoice);

        List<StockAvailabilityGateway.AllocationData> allocations = saved
                .getLines().stream()
                .flatMap(line -> line.getAllocations().stream()
                        .map(a -> new StockAvailabilityGateway.AllocationData(
                                a.getWarehouseId(), line.getProductId(),
                                a.getAllocated().intValue())))
                .toList();

        List<StockAvailabilityGateway.EnrichedStock> enriched = stockEnrichmentService
                .enrich(allocations);

        Map<InvoiceMapper.WarehouseProductKey, InvoiceMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockAvailabilityGateway.EnrichedStock es : enriched) {
            enrichmentMap.put(
                    new InvoiceMapper.WarehouseProductKey(es.warehouseId(),
                            es.productId()),
                    new InvoiceMapper.WarehouseEnrichment(es.warehouseCode(),
                            es.warehouseName(), es.availableStock()));
        }

        activityNotifications.record(ActivityEvent.of("FINANCE_BILLING_INVOICES",
                "EDIT", "INVOICE", saved.getId().value(),
                saved.getInvoiceNumber().value(), null));
        return InvoiceMapper.toDetailResponse(saved, enrichmentMap, Map.of());
    }

    private void revertStockAllocations(Invoice invoice) {
        inventoryGateway.releaseInvoiceReservations(
                InventoryGateway.InvoiceInventoryCommand.from(invoice));
    }

    private List<InvoiceLineAllocation> applyStockAllocations(
            UpdateInvoiceRequest.InvoiceLineRequest lineRequest,
            UUID productId) {
        inventoryGateway
                .reserveAllocations(lineRequest.warehouseAllocations().stream()
                        .map(a -> new InventoryGateway.Allocation(
                                a.warehouseId(), productId,
                                a.allocated().intValue()))
                        .toList());
        return lineRequest.warehouseAllocations().stream()
                .map(a -> InvoiceLineAllocation.create(a.warehouseId(),
                        a.allocated()))
                .toList();
    }
}
