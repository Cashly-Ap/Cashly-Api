package com.cashly.cashly_api.quotes.application.services;

import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentQuery;
import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentSnapshot;
import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentRow;
import com.cashly.cashly_api.quotes.application.ports.in.QuoteDocumentFilter;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.application.usecases.GetQuotesUseCase;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;

import java.util.List;
import java.util.UUID;

public class QuoteDocumentQueryService implements QuoteDocumentQuery {
    private final QuoteRepository repository;
    private final GetQuotesUseCase listQuery;
    public QuoteDocumentQueryService(QuoteRepository repository, GetQuotesUseCase listQuery) {
        this.repository = repository;
        this.listQuery = listQuery;
    }
    public QuoteDocumentSnapshot find(UUID id) {
        var entity = repository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));
        var data = QuoteMapper.toDetailResponse(entity);
        return new QuoteDocumentSnapshot(data.id(), data.quoteNumber(), data.customerName(), data.customerRif(), data.status(),
                data.notes(), data.cancelledReason(), data.createdAt(), data.subtotal(),
                data.lines().stream().map(line -> new QuoteDocumentSnapshot.Line(line.productCode(), line.productName(), line.quantity(), line.unitPrice(), line.lineTotal())).toList());
    }
    public List<QuoteDocumentRow> findAll(QuoteDocumentFilter filter) {
        return listQuery.execute(filter.dateFrom(), filter.dateTo(), filter.customerId(), filter.status(), filter.productId(), filter.categoryId(), "createdAt", "desc")
                .stream().map(data -> new QuoteDocumentRow(data.id(), data.quoteNumber(), data.customerName(), data.status(), data.subtotal(), data.createdAt())).toList();
    }
}