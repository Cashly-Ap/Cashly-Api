package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteListResponse;
import com.parsun.parsun_api.quotes.application.mappers.QuoteMapper;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;

import java.util.List;

public class GetQuotesUseCase {

    private final QuoteRepository quoteRepository;

    public GetQuotesUseCase(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    public List<QuoteListResponse> execute() {
        return quoteRepository.findAll()
                .stream()
                .map(QuoteMapper::toListResponse)
                .toList();
    }
}
