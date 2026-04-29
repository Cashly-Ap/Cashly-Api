package com.parsun.parsun_api.price_types.infrastructure.persistence.repositories;

import com.parsun.parsun_api.price_types.domain.entities.PriceType;
import com.parsun.parsun_api.price_types.domain.repositories.PriceTypeRepository;
import com.parsun.parsun_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaPriceTypeRepository implements PriceTypeRepository {

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
    public List<PriceType> findAll() {
        return springDataRepository.findAll().stream()
            .map(PriceTypeEntity::toDomain)
            .toList();
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
    public boolean isInUseByProducts(UUID id) {
        return springDataRepository.isInUseByProducts(id);
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }
}
