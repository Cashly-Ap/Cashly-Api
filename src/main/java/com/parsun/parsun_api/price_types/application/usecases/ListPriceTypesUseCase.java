package com.parsun.parsun_api.price_types.application.usecases;

import com.parsun.parsun_api.price_types.application.dto.response.PriceTypeResponse;
import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListPriceTypesUseCase {

    private final PriceTypeRepository priceTypeRepository;

    public ListPriceTypesUseCase(PriceTypeRepository priceTypeRepository) {
        this.priceTypeRepository = priceTypeRepository;
    }

    public List<PriceTypeResponse> execute(String orderBy, String orderDir) {
        List<PriceType> priceTypes = priceTypeRepository.findAll(orderBy, orderDir);
        Set<UUID> inUseIds = priceTypeRepository.findIdsInUseByProducts(
            priceTypes.stream().map(PriceType::getId).collect(Collectors.toSet())
        );
        return priceTypes.stream()
            .map(pt -> PriceTypeResponse.from(pt, !inUseIds.contains(pt.getId())))
            .toList();
    }
}
