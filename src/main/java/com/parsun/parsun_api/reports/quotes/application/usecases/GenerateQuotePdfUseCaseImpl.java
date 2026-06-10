package com.parsun.parsun_api.reports.quotes.application.usecases;

import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.mappers.QuoteMapper;
import com.parsun.parsun_api.reports.quotes.application.port.QuotePdfPort;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;

import java.util.UUID;


public class GenerateQuotePdfUseCaseImpl implements GenerateQuotePdfUseCase {

    private final QuoteRepository quoteRepository;
    private final QuotePdfPort quotePdfPort;

    public GenerateQuotePdfUseCaseImpl(QuoteRepository quoteRepository,
                                        QuotePdfPort quotePdfPort) {
        this.quoteRepository = quoteRepository;
        this.quotePdfPort = quotePdfPort;
    }

    @Override
    public byte[] execute(UUID quoteId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        QuoteDetailResponse quoteDetail = QuoteMapper.toDetailResponse(quote);

        return quotePdfPort.generatePdf(quoteDetail);
    }
}
