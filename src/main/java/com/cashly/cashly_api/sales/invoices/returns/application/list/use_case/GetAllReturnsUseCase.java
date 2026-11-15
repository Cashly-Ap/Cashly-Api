package com.cashly.cashly_api.sales.invoices.returns.application.list.use_case;

import com.cashly.cashly_api.sales.invoices.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.sales.invoices.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.sales.invoices.returns.domain.repository.ReturnRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetAllReturnsUseCase {

    private final ReturnRepository returnRepository;
    private final ReturnResponseAssembler responses;

    public GetAllReturnsUseCase(ReturnRepository returnRepository,
            ReturnResponseAssembler responses) {
        this.returnRepository = returnRepository;
        this.responses = responses;
    }

    public List<ReturnListResponse> execute(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, UUID invoiceId) {
        return returnRepository
                .findAllWithFilters(dateFrom, dateTo, customerId, invoiceId)
                .stream().map(responses::toListResponse).toList();
    }

    public PageResponse<ReturnListResponse> executePage(LocalDate dateFrom,
            LocalDate dateTo, UUID customerId, UUID invoiceId, String search,
            String returnType, int page, int size, String orderBy,
            String orderDir) {
        PageResponse<com.cashly.cashly_api.sales.invoices.returns.domain.entity.Return> returns = returnRepository
                .findPageWithFilters(dateFrom, dateTo, customerId, invoiceId,
                        search, returnType, page, size, orderBy, orderDir);
        List<ReturnListResponse> content = returns.content().stream()
                .map(responses::toListResponse).toList();
        return new PageResponse<>(content, returns.totalElements(),
                returns.totalPages(), returns.size(), returns.number(),
                returns.first(), returns.last(), content.isEmpty());
    }
}
