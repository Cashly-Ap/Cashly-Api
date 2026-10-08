package com.cashly.cashly_api.price_types.infrastructure.persistence.repositories;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.price_types.domain.entities.PriceType;
import com.cashly.cashly_api.price_types.domain.repositories.PriceTypeRepository;
import com.cashly.cashly_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaPriceTypeRepository implements PriceTypeRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of(
        "name", "active", "createdAt", "updatedAt"
    );

    private final SpringDataPriceTypeRepository springDataRepository;

    public JpaPriceTypeRepository(SpringDataPriceTypeRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PriceType save(PriceType priceType) {
        if (priceType.getId() != null) {
            return springDataRepository.findById(priceType.getId())
                .map(entity -> {
                    entity.updateFromDomain(priceType);
                    return springDataRepository.save(entity).toDomain();
                })
                .orElseGet(() -> springDataRepository.save(PriceTypeEntity.fromDomain(priceType)).toDomain());
        }
        PriceTypeEntity entity = PriceTypeEntity.fromDomain(priceType);
        entity.setId(UUID.randomUUID());
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public Optional<PriceType> findById(UUID id) {
        return springDataRepository.findById(id).map(PriceTypeEntity::toDomain);
    }

    @Override
    public PageResponse<PriceType> findAllWithFilters(String search, Boolean active,
                                                        int page, int size,
                                                        String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "createdAt";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<PriceType> result = springDataRepository.findAllWithFilters(search, active, pageable)
            .map(PriceTypeEntity::toDomain);

        return PageResponseMapper.from(result);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID id) {
        return springDataRepository.existsByNameAndIdNot(name, id);
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }
}
