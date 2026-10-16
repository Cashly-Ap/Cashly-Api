package com.cashly.cashly_api.invoices.application.get;

import com.cashly.cashly_api.invoices.application.ports.out.InvoiceNoteDetailsGateway;
import com.cashly.cashly_api.invoices.application.ports.out.InvoiceReturnDetailsGateway;
import com.cashly.cashly_api.invoices.application.ports.out.StockAvailabilityGateway;
import com.cashly.cashly_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.cashly.cashly_api.invoices.application.mappers.InvoiceMapper;
import com.cashly.cashly_api.invoices.domain.Invoice;
import com.cashly.cashly_api.invoices.domain.InvoiceLine;
import com.cashly.cashly_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.invoices.domain.InvoiceRepository;
import com.cashly.cashly_api.invoices.domain.valueobjects.InvoiceId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetInvoiceByIdUseCase {
    private final InvoiceRepository invoiceRepository;
    private final StockAvailabilityGateway stockAvailability;
    private final InvoiceNoteDetailsGateway noteDetails;
    private final InvoiceReturnDetailsGateway returnDetails;

    public GetInvoiceByIdUseCase(InvoiceRepository invoiceRepository, StockAvailabilityGateway stockAvailability,
                                 InvoiceNoteDetailsGateway noteDetails, InvoiceReturnDetailsGateway returnDetails) {
        this.invoiceRepository = invoiceRepository;
        this.stockAvailability = stockAvailability;
        this.noteDetails = noteDetails;
        this.returnDetails = returnDetails;
    }

    public InvoiceDetailResponse execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));
        List<InvoiceNoteDetailsGateway.Note> creditNotes = noteDetails.findCreditNotes(id);
        List<InvoiceNoteDetailsGateway.Note> debitNotes = noteDetails.findDebitNotes(id);
        BigDecimal creditApplied = creditNotes.stream().map(InvoiceNoteDetailsGateway.Note::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal debitApplied = debitNotes.stream().map(InvoiceNoteDetailsGateway.Note::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<InvoiceDetailResponse.NoteInfo> notes = java.util.stream.Stream.concat(
                creditNotes.stream().map(note -> toNoteInfo(note, "CREDIT")),
                debitNotes.stream().map(note -> toNoteInfo(note, "DEBIT"))).toList();
        List<StockAvailabilityGateway.AllocationData> allocations = invoice.getLines().stream()
                .flatMap(line -> line.getAllocations().stream().map(allocation ->
                        new StockAvailabilityGateway.AllocationData(allocation.getWarehouseId(),
                                line.getProductId(), allocation.getAllocated().intValue())))
                .toList();
        Map<InvoiceMapper.WarehouseProductKey, Integer> thisInvoiceAllocations = new HashMap<>();
        for (StockAvailabilityGateway.AllocationData allocation : allocations) {
            InvoiceMapper.WarehouseProductKey key = new InvoiceMapper.WarehouseProductKey(
                    allocation.warehouseId(), allocation.productId());
            thisInvoiceAllocations.merge(key, allocation.allocated(), Integer::sum);
        }
        Map<InvoiceMapper.WarehouseProductKey, InvoiceMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockAvailabilityGateway.EnrichedStock stock : stockAvailability.enrich(allocations)) {
            enrichmentMap.put(new InvoiceMapper.WarehouseProductKey(stock.warehouseId(), stock.productId()),
                    new InvoiceMapper.WarehouseEnrichment(stock.warehouseCode(), stock.warehouseName(),
                            stock.availableStock()));
        }
        List<UUID> productIds = invoice.getLines().stream().map(InvoiceLine::getProductId).toList();
        Map<UUID, List<StockAvailabilityGateway.WarehouseStockInfo>> productStocks =
                stockAvailability.getAllStockByProducts(invoice.getCustomerId(), productIds);
        List<InvoiceDetailResponse.ReturnDetail> returns = returnDetails.findByInvoiceId(id).stream()
                .map(this::toReturnDetail).toList();
        return InvoiceMapper.toDetailResponse(invoice, enrichmentMap, productStocks, thisInvoiceAllocations,
                creditApplied, debitApplied, notes, returns);
    }

    private InvoiceDetailResponse.NoteInfo toNoteInfo(InvoiceNoteDetailsGateway.Note note, String type) {
        return new InvoiceDetailResponse.NoteInfo(note.id(), note.number(), type,
                note.amount(), note.reason(), note.createdAt());
    }

    private InvoiceDetailResponse.ReturnDetail toReturnDetail(InvoiceReturnDetailsGateway.ReturnDetails item) {
        List<InvoiceDetailResponse.ReturnLineDetail> lines = item.lines().stream().map(line ->
                new InvoiceDetailResponse.ReturnLineDetail(line.productId(), line.productName(), line.productCode(),
                        line.quantity(), line.unitPrice(), line.subtotal(), line.allocations().stream().map(allocation ->
                        new InvoiceDetailResponse.ReturnLineAllocationDetail(allocation.warehouseId(),
                                allocation.warehouseName(), allocation.allocated())).toList())).toList();
        return new InvoiceDetailResponse.ReturnDetail(item.id(), item.number(), item.type(), item.reason(),
                item.total(), item.createdAt(), item.createdByName(), lines);
    }
}
