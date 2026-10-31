package com.cashly.cashly_api.finance.billing.returns.application.queries.services;

import com.cashly.cashly_api.finance.billing.returns.application.ports.in.InvoiceReturnDetailsQuery;
import com.cashly.cashly_api.finance.billing.returns.domain.repository.ReturnRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvoiceReturnDetailsQueryService
        implements
            InvoiceReturnDetailsQuery {
    private final ReturnRepository returns;

    public InvoiceReturnDetailsQueryService(ReturnRepository returns) {
        this.returns = returns;
    }

    @Override
    public List<ReturnDetails> findByInvoiceId(UUID invoiceId) {
        return returns.findAllWithFilters(null, null, null, invoiceId).stream()
                .map(item -> new ReturnDetails(item.getId().value(),
                        item.getReturnNumber().value(),
                        item.getReturnType().name(), item.getReason(),
                        item.getTotal(), item.getCreatedAt(),
                        item.getCreatedByName(),
                        item.getLines().stream().map(line -> new Line(
                                line.getProductId(), line.getQuantity(),
                                line.getUnitPrice(), line.getSubtotal(),
                                line.getAllocations().stream()
                                        .map(allocation -> new Allocation(
                                                allocation.getWarehouseId(),
                                                allocation.getAllocated()))
                                        .toList()))
                                .toList()))
                .toList();
    }
}
