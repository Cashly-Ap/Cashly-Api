package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.adapters;

import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.repositories.SpringDataWarehouseStockRepository;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.entity.WarehouseStock;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.repository.WarehouseStockRepository;
import com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.entities.WarehouseStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.NoSuchElementException;

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
    public boolean existsByWarehouseIdAndProductId(UUID warehouseId, UUID productId) {
        return springDataRepository.existsByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Override
    public Optional<WarehouseStock> findByWarehouseIdAndProductIdForUpdate(UUID warehouseId, UUID productId) {
        return springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)
                .map(WarehouseStockEntity::toDomain);
    }

    @Override
    public WarehouseStock findOrCreateByWarehouseIdAndProductIdForUpdate(UUID warehouseId, UUID productId) {
        springDataRepository.insertIfMissing(UUID.randomUUID().toString(), warehouseId.toString(), productId.toString());
        return springDataRepository.findByWarehouseIdAndProductIdForUpdate(warehouseId, productId)
                .map(WarehouseStockEntity::toDomain)
                .orElseThrow(() -> new NoSuchElementException("Stock row was not available after creation"));
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
        return PageResponseMapper.from(result.map(WarehouseStockEntity::toDomain));
    }

    @Override
    public List<WarehouseStock> findByProductId(UUID productId) {
        return springDataRepository.findByProductId(productId)
                .stream().map(WarehouseStockEntity::toDomain).toList();
    }

    @Override
    public List<WarehouseStock> findAllByProductIdIn(Set<UUID> productIds) {
        return springDataRepository.findAllByProductIdIn(productIds)
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
