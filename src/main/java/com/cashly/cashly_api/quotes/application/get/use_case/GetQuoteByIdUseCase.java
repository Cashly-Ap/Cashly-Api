package com.cashly.cashly_api.quotes.application.get.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.entity.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetQuoteByIdUseCase {

    private final QuoteRepository quoteRepository;
    private final QuoteWarehouseGateway warehouses;

    public GetQuoteByIdUseCase(QuoteRepository quoteRepository, QuoteWarehouseGateway warehouses) {
        this.quoteRepository = quoteRepository;
        this.warehouses = warehouses;
    }

    public QuoteDetailResponse execute(UUID id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        Map<UUID, List<QuoteMapper.WarehouseStockInfo>> stockByProduct = quote.getLines().stream()
                .map(QuoteLine::getProductId)
                .distinct()
                .collect(Collectors.toMap(productId -> productId, this::fetchStockForProduct));

        return QuoteMapper.toDetailResponse(quote, stockByProduct);
    }

    private List<QuoteMapper.WarehouseStockInfo> fetchStockForProduct(UUID productId) {
        return warehouses.findStockByProductId(productId).stream()
                .map(stock -> new QuoteMapper.WarehouseStockInfo(stock.warehouseId(), stock.warehouseName(),
                        stock.available()))
                .toList();
    }
}
