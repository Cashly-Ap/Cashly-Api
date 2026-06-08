package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaWarehouseStockRepository implements WarehouseStockRepository {

    private final SpringDataWarehouseStockRepository springDataRepository;

    public JpaWarehouseStockRepository(SpringDataWarehouseStockRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public WarehouseStock save(WarehouseStock stock) {
        if (stock.getId() != null) {
            return springDataRepository.findById(stock.getId())
                    .map(entity -> {
                        entity.updateFromDomain(stock);
                        return springDataRepository.save(entity).toDomain();
                    })
                    .orElseGet(() -> springDataRepository.save(WarehouseStockEntity.fromDomain(stock)).toDomain());
        }
        return springDataRepository.save(WarehouseStockEntity.fromDomain(stock)).toDomain();
    }

    @Override
    public Optional<WarehouseStock> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId) {
        return springDataRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .map(WarehouseStockEntity::toDomain);
    }

    @Override
    public Optional<WarehouseStock> findByWarehouseIdAndProductIdForUpdate(UUID warehouseId, UUID productId) {
        return springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)
                .map(WarehouseStockEntity::toDomain);
    }

    @Override
    public List<WarehouseStock> findByWarehouseId(UUID warehouseId) {
        return springDataRepository.findByWarehouseId(warehouseId, PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()))
                .stream().map(WarehouseStockEntity::toDomain).toList();
    }

    @Override
    public PageResponse<WarehouseStock> findByWarehouseIdPaged(UUID warehouseId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("productId").ascending());
        Page<WarehouseStockEntity> result = springDataRepository.findByWarehouseId(warehouseId, pageable);
        return PageResponse.from(result.map(WarehouseStockEntity::toDomain));
    }

    @Override
    public List<WarehouseStock> findByProductId(UUID productId) {
        return springDataRepository.findByProductId(productId)
                .stream().map(WarehouseStockEntity::toDomain).toList();
    }

    @Override
    public boolean existsByProductId(UUID productId) {
        return springDataRepository.existsByProductId(productId);
    }

    @Override
    public void deleteByProductId(UUID productId) {
        springDataRepository.deleteByProductId(productId);
    }
}
