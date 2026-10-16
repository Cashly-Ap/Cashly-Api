package com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.adapters;

import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.repositories.SpringDataStockMovementRepository;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovement;
import com.cashly.cashly_api.warehouses.stock.domain.MovementType;
import com.cashly.cashly_api.warehouses.stock.domain.StockMovementRepository;
import com.cashly.cashly_api.warehouses.stock.infrastructure.persistence.entities.StockMovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JpaStockMovementRepository implements StockMovementRepository {

    private final SpringDataStockMovementRepository springDataRepository;

    public JpaStockMovementRepository(SpringDataStockMovementRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PageResponse<StockMovement> findByWarehouseIdAndMovementType(
            UUID warehouseId, MovementType movementType, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementEntity> result = springDataRepository.findByWarehouseIdAndMovementType(
                warehouseId, movementType, pageable);
        return PageResponseMapper.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public PageResponse<StockMovement> findByWarehouseIdAndProductIdAndMovementType(
            UUID warehouseId, UUID productId, MovementType movementType, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementEntity> result =
                springDataRepository.findByWarehouseIdAndProductIdAndMovementType(
                        warehouseId, productId, movementType, pageable);
        return PageResponseMapper.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementEntity entity = StockMovementEntity.fromDomain(movement);
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public PageResponse<StockMovement> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId,
                                                                       int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementEntity> result =
                springDataRepository.findByWarehouseIdAndProductId(warehouseId, productId, pageable);
        return PageResponseMapper.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public PageResponse<StockMovement> findByWarehouseId(UUID warehouseId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementEntity> result = springDataRepository.findByWarehouseId(warehouseId, pageable);
        return PageResponseMapper.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public void deleteByProductId(UUID productId) {
        springDataRepository.deleteByProductId(productId);
    }
}
