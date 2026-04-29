package com.parsun.parsun_api.warehouses.infrastructure.persistence.repositories;

import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.infrastructure.persistence.entities.StockMovementEntity;
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
        return PageResponse.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public PageResponse<StockMovement> findByWarehouseId(UUID warehouseId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<StockMovementEntity> result = springDataRepository.findByWarehouseId(warehouseId, pageable);
        return PageResponse.from(result.map(StockMovementEntity::toDomain));
    }

    @Override
    public void deleteByProductId(UUID productId) {
        springDataRepository.deleteByProductId(productId);
    }
}
