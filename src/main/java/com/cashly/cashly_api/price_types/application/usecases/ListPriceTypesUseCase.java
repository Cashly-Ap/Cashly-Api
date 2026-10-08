package com.cashly.cashly_api.price_types.application.usecases;

import com.cashly.cashly_api.price_types.application.dto.response.PriceTypeResponse;
import com.cashly.cashly_api.price_types.domain.entities.PriceType;
import com.cashly.cashly_api.price_types.application.contracts.PriceTypeUsageGateway;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListPriceTypesUseCase {

    private final PriceTypeUsageGateway usageGateway;
    private final PriceTypeRepository priceTypeRepository;

    public ListPriceTypesUseCase(PriceTypeUsageGateway usageGateway, PriceTypeRepository priceTypeRepository) {
        this.usageGateway = usageGateway;
        this.priceTypeRepository = priceTypeRepository;
    }

    public PageResponse<PriceTypeResponse> execute(String search, Boolean active,
                                                     int page, int size,
                                                     String orderBy, String orderDir) {
        PageResponse<PriceType> priceTypePage = priceTypeRepository.findAllWithFilters(
            search, active, page, size, orderBy, orderDir);

        Set<UUID> inUseIds = usageGateway.findIdsInUseByProducts(
            priceTypePage.content().stream().map(PriceType::getId).collect(Collectors.toSet())
        );

        List<PriceTypeResponse> content = priceTypePage.content().stream()
            .map(pt -> PriceTypeResponse.from(pt, !inUseIds.contains(pt.getId())))
            .toList();

        return new PageResponse<>(content, priceTypePage.totalElements(), priceTypePage.totalPages(),
            priceTypePage.size(), priceTypePage.number(), priceTypePage.first(),
            priceTypePage.last(), priceTypePage.empty());
    }
}
