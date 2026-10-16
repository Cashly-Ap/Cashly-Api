package com.cashly.cashly_api.suppliers.infrastructure.persistence.adapters;

import com.cashly.cashly_api.suppliers.infrastructure.persistence.repositories.SpringDataSupplierRepository;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.suppliers.infrastructure.persistence.entities.SupplierEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Collection;

@Component
public class JpaSupplierRepository implements SupplierRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("legalName", "code", "rif", "alias", "type");

    private final SpringDataSupplierRepository springDataRepository;

    public JpaSupplierRepository(SpringDataSupplierRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Supplier save(Supplier supplier) {
        Optional<SupplierEntity> existing = springDataRepository.findById(supplier.getId().getValue());

        SupplierEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.updateFromDomain(supplier);
        } else {
            entity = SupplierEntity.fromDomain(supplier);
        }

        SupplierEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Supplier> findById(SupplierId id) {
        return springDataRepository.findById(id.getValue())
            .map(SupplierEntity::toDomain);
    }

    @Override
    public List<Supplier> findAllById(Collection<SupplierId> ids) {
        List<java.util.UUID> values = ids.stream().map(SupplierId::getValue).toList();
        return springDataRepository.findAllById(values).stream()
                .map(SupplierEntity::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findAll() {
        return springDataRepository.findAll().stream()
            .map(SupplierEntity::toDomain)
            .toList();
    }

    @Override
    public PageResponse<Supplier> findAllWithFilters(String search, String type, Boolean active,
                                                      String state, int page, int size,
                                                      String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "legalName";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<SupplierEntity> result = springDataRepository.findAllWithFilters(
            search, type, active, state, pageable);

        Page<Supplier> mapped = result.map(SupplierEntity::toDomain);
        return PageResponseMapper.from(mapped);
    }

    @Override
    public void delete(SupplierId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(SupplierId id) {
        return springDataRepository.existsById(id.getValue());
    }

    @Override
    public long getNextCode() {
        return springDataRepository.findMaxCode().orElse(0L) + 1;
    }
}
