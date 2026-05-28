package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.credit_notes.domain.entities.CreditNote;
import com.parsun.parsun_api.debit_notes.domain.entities.DebitNote;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GetInvoiceByIdUseCase {

    private final InvoiceRepository invoiceRepository;
    private final StockEnrichmentService stockEnrichmentService;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;

    public GetInvoiceByIdUseCase(InvoiceRepository invoiceRepository,
                                  StockEnrichmentService stockEnrichmentService,
                                  CreditNoteRepository creditNoteRepository,
                                  DebitNoteRepository debitNoteRepository) {
        this.invoiceRepository = invoiceRepository;
        this.stockEnrichmentService = stockEnrichmentService;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
    }

    public InvoiceDetailResponse execute(UUID id) {
        Invoice invoice = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        List<CreditNote> creditNotes = creditNoteRepository.findByInvoiceId(id);
        List<DebitNote> debitNotes = debitNoteRepository.findByInvoiceId(id);

        BigDecimal creditApplied = creditNotes.stream()
                .map(CreditNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal debitApplied = debitNotes.stream()
                .map(DebitNote::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InvoiceDetailResponse.NoteInfo> notes = new ArrayList<>();
        for (CreditNote cn : creditNotes) {
            notes.add(new InvoiceDetailResponse.NoteInfo(
                    cn.getId().value(), "CREDIT",
                    cn.getAmount(), cn.getReason(), cn.getCreatedAt()
            ));
        }
        for (DebitNote dn : debitNotes) {
            notes.add(new InvoiceDetailResponse.NoteInfo(
                    dn.getId().value(), "DEBIT",
                    dn.getAmount(), dn.getReason(), dn.getCreatedAt()
            ));
        }

        List<StockEnrichmentService.AllocationData> allocations = invoice.getLines().stream()
                .flatMap(line -> line.getAllocations().stream()
                        .map(a -> new StockEnrichmentService.AllocationData(
                                a.getWarehouseId(),
                                line.getProductId(),
                                a.getAllocated().intValue()
                        )))
                .toList();

        Map<InvoiceMapper.WarehouseProductKey, Integer> thisInvoiceAllocations = new HashMap<>();
        for (StockEnrichmentService.AllocationData ad : allocations) {
            InvoiceMapper.WarehouseProductKey key = new InvoiceMapper.WarehouseProductKey(ad.warehouseId(), ad.productId());
            thisInvoiceAllocations.merge(key, ad.allocated(), Integer::sum);
        }

        List<StockEnrichmentService.EnrichedStock> enriched = stockEnrichmentService.enrich(allocations);

        Map<InvoiceMapper.WarehouseProductKey, InvoiceMapper.WarehouseEnrichment> enrichmentMap = new HashMap<>();
        for (StockEnrichmentService.EnrichedStock es : enriched) {
            enrichmentMap.put(
                    new InvoiceMapper.WarehouseProductKey(es.warehouseId(), es.productId()),
                    new InvoiceMapper.WarehouseEnrichment(es.warehouseCode(), es.warehouseName(), es.availableStock())
            );
        }

        List<UUID> productIds = invoice.getLines().stream()
                .map(InvoiceLine::getProductId)
                .toList();
        Map<UUID, List<StockEnrichmentService.WarehouseStockInfo>> productStocks =
                stockEnrichmentService.getAllStockByProducts(invoice.getCustomerId(), productIds);

        return InvoiceMapper.toDetailResponse(invoice, enrichmentMap, productStocks, thisInvoiceAllocations,
                creditApplied, debitApplied, notes);
    }
}
