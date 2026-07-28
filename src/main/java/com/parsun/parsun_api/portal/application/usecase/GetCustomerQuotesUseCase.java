package com.parsun.parsun_api.portal.application.usecase;

import com.parsun.parsun_api.portal.application.dto.response.PortalQuoteDetailResponse;
import com.parsun.parsun_api.portal.application.dto.response.PortalQuoteListResponse;
import com.parsun.parsun_api.portal.application.mapper.PortalQuoteMapper;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;

import java.util.List;
import java.util.UUID;

public class GetCustomerQuotesUseCase {

    private final QuoteRepository quoteRepository;

    public GetCustomerQuotesUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public List<PortalQuoteListResponse> execute(String customerId) {
        UUID customerUuid = UUID.fromString(customerId);
        return quoteRepository.findAllWithFilters(null, null, customerUuid, null, null, null, "createdAt", "desc")
            .stream()
            .map(PortalQuoteMapper::toListResponse)
            .toList();
    }

    public List<PortalQuoteListResponse> execute(String customerId, String search, String status) {
        UUID customerUuid = UUID.fromString(customerId);
        return quoteRepository.findAllWithFilters(null, null, customerUuid, status, null, null, "createdAt", "desc")
            .stream()
            .filter(quote -> search == null || search.isBlank()
                || quote.getQuoteNumber().value().toLowerCase().contains(search.toLowerCase()))
            .map(PortalQuoteMapper::toListResponse)
            .toList();
    }

    public PortalQuoteDetailResponse execute(String customerId, String quoteId) {
        UUID customerUuid = UUID.fromString(customerId);
        UUID quoteUuid = UUID.fromString(quoteId);

        var quote = quoteRepository.findById(quoteUuid)
            .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (!quote.getCustomerId().equals(customerUuid)) {
            throw new IllegalArgumentException("Quote not found");
        }

        return PortalQuoteMapper.toDetailResponse(quote);
    }
}
