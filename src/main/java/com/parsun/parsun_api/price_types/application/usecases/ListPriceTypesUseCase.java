package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListPriceTypesUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public ListPriceTypesUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    public PageResponse<PriceTypeResponse> execute(String search, Boolean active,
                                                     int page, int size,
                                                     String orderBy, String orderDir) {
        PageResponse<PriceType> priceTypePage = priceTypeRepository.findAllWithFilters(
            search, active, page, size, orderBy, orderDir);

        Set<UUID> inUseIds = priceTypeRepository.findIdsInUseByProducts(
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
