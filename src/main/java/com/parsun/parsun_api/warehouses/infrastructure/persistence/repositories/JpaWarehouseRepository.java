package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaWarehouseRepository implements WarehouseRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("code", "name", "state", "createdAt");

    private final SpringDataWarehouseRepository springDataRepository;

    public JpaWarehouseRepository(SpringDataWarehouseRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        if (warehouse.getId() != null) {
            return springDataRepository.findById(warehouse.getId())
                    .map(entity -> {
                        entity.updateFromDomain(warehouse);
                        return springDataRepository.save(entity).toDomain();
                    })
                    .orElseGet(() -> springDataRepository.save(WarehouseEntity.fromDomain(warehouse)).toDomain());
        }
        return springDataRepository.save(WarehouseEntity.fromDomain(warehouse)).toDomain();
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return springDataRepository.findById(id).map(WarehouseEntity::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return springDataRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public PageResponse<Warehouse> findAllWithFilters(String search, Boolean active,
                                                       int page, int size,
                                                       String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "name";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<WarehouseEntity> result = springDataRepository.findAllWithFilters(search, active, pageable);
        return PageResponse.from(result.map(WarehouseEntity::toDomain));
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }
}
