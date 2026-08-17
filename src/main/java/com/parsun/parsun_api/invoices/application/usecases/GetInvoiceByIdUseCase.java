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
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnLineAllocation;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetInvoiceByIdUseCase {

    private final InvoiceRepository invoiceRepository;
    private final StockEnrichmentService stockEnrichmentService;
    private final CreditNoteRepository creditNoteRepository;
    private final DebitNoteRepository debitNoteRepository;
    private final ReturnRepository returnRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public GetInvoiceByIdUseCase(InvoiceRepository invoiceRepository,
                                  StockEnrichmentService stockEnrichmentService,
                                  CreditNoteRepository creditNoteRepository,
                                  DebitNoteRepository debitNoteRepository,
                                  ReturnRepository returnRepository,
                                  ProductRepository productRepository,
                                  WarehouseRepository warehouseRepository) {
        this.invoiceRepository = invoiceRepository;
        this.stockEnrichmentService = stockEnrichmentService;
        this.creditNoteRepository = creditNoteRepository;
        this.debitNoteRepository = debitNoteRepository;
        this.returnRepository = returnRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
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
                    cn.getId().value(), cn.getNoteNumber().value(), "CREDIT",
                    cn.getAmount(), cn.getReason(), cn.getCreatedAt()
            ));
        }
        for (DebitNote dn : debitNotes) {
            notes.add(new InvoiceDetailResponse.NoteInfo(
                    dn.getId().value(), dn.getNoteNumber().value(), "DEBIT",
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

        
        List<Return> returns = returnRepository.findAllWithFilters(null, null, null, id);
        List<InvoiceDetailResponse.ReturnDetail> returnDetails = buildReturnDetails(returns);

        return InvoiceMapper.toDetailResponse(invoice, enrichmentMap, productStocks, thisInvoiceAllocations,
                creditApplied, debitApplied, notes, returnDetails);
    }

    private List<InvoiceDetailResponse.ReturnDetail> buildReturnDetails(List<Return> returns) {
        List<InvoiceDetailResponse.ReturnDetail> details = new ArrayList<>();

        for (Return ret : returns) {
            List<InvoiceDetailResponse.ReturnLineDetail> lineDetails = new ArrayList<>();

            for (ReturnLine line : ret.getLines()) {
                String productName = "";
                String productCode = "";

                Optional<Product> productOpt = productRepository.findById(line.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    productName = product.getName();
                    productCode = product.getCode();
                }

                List<InvoiceDetailResponse.ReturnLineAllocationDetail> allocationDetails = new ArrayList<>();
                for (ReturnLineAllocation alloc : line.getAllocations()) {
                    String warehouseName = "";
                    Optional<Warehouse> whOpt = warehouseRepository.findById(alloc.getWarehouseId());
                    if (whOpt.isPresent()) {
                        warehouseName = whOpt.get().getName();
                    }
                    allocationDetails.add(new InvoiceDetailResponse.ReturnLineAllocationDetail(
                            alloc.getWarehouseId(),
                            warehouseName,
                            alloc.getAllocated()
                    ));
                }

                lineDetails.add(new InvoiceDetailResponse.ReturnLineDetail(
                        line.getProductId(),
                        productName,
                        productCode,
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getSubtotal(),
                        allocationDetails
                ));
            }

            details.add(new InvoiceDetailResponse.ReturnDetail(
                    ret.getId().value(),
                    ret.getReturnNumber().value(),
                    ret.getReturnType().name(),
                    ret.getReason(),
                    ret.getTotal(),
                    ret.getCreatedAt(),
                    ret.getCreatedByName(),
                    lineDetails
            ));
        }

        return details;
    }
}
