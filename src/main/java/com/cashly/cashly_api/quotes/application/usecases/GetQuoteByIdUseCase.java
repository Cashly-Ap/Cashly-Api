package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.quotes.application.dto.response.QuoteDetailResponse;
import com.cashly.cashly_api.quotes.application.mappers.QuoteMapper;
import com.cashly.cashly_api.quotes.domain.entities.Quote;
import com.cashly.cashly_api.quotes.domain.entities.QuoteLine;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.repositories.QuoteRepository;
import com.cashly.cashly_api.warehouses.domain.entities.WarehouseStock;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseStockRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GetQuoteByIdUseCase {

    private final QuoteRepository quoteRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;

    public GetQuoteByIdUseCase(QuoteRepository quoteRepository,
                               WarehouseStockRepository warehouseStockRepository,
                               WarehouseRepository warehouseRepository) {
        this.quoteRepository = quoteRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public QuoteDetailResponse execute(UUID id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));

        Map<UUID, List<QuoteMapper.WarehouseStockInfo>> stockByProduct = buildStockByProduct(quote.getLines());

        return QuoteMapper.toDetailResponse(quote, stockByProduct);
    }

    private Map<UUID, List<QuoteMapper.WarehouseStockInfo>> buildStockByProduct(List<QuoteLine> lines) {
        return lines.stream()
                .map(QuoteLine::getProductId)
                .distinct()
                .collect(Collectors.toMap(
                        productId -> productId,
                        this::fetchStockForProduct
                ));
    }

    private List<QuoteMapper.WarehouseStockInfo> fetchStockForProduct(UUID productId) {
        List<WarehouseStock> stocks = warehouseStockRepository.findByProductId(productId);
        return stocks.stream()
                .map(stock -> new QuoteMapper.WarehouseStockInfo(
                        stock.getWarehouseId(),
                        warehouseRepository.findById(stock.getWarehouseId())
                                .map(w -> w.getName())
                                .orElse("Unknown"),
                        stock.getAvailableQuantity()
                ))
                .toList();
    }
}
