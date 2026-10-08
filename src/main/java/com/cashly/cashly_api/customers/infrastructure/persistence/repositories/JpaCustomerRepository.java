package com.cashly.cashly_api.customers.infrastructure.persistence.repositories;

import com.cashly.cashly_api.shared.infrastructure.pagination.PageResponseMapper;

import com.cashly.cashly_api.customers.domain.entities.Customer;
import com.cashly.cashly_api.customers.domain.repositories.CustomerRepository;
import com.cashly.cashly_api.customers.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.customers.domain.valueobjects.Rif;
import com.cashly.cashly_api.customers.infrastructure.persistence.entities.CustomerEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaCustomerRepository implements CustomerRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("legalName", "code", "rif", "alias", "type");

    private final SpringDataCustomerRepository springDataRepository;

    public JpaCustomerRepository(SpringDataCustomerRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Customer save(Customer customer) {
        Optional<CustomerEntity> existing = springDataRepository.findById(customer.getId().getValue());

        CustomerEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.updateFromDomain(customer);
        } else {
            entity = CustomerEntity.fromDomain(customer);
        }

        CustomerEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return springDataRepository.findById(id.getValue())
            .map(CustomerEntity::toDomain);
    }

    @Override
    public Optional<Customer> findByRif(Rif rif) {
        return springDataRepository.findByRif(rif.getValue())
            .map(CustomerEntity::toDomain);
    }

    @Override
    public boolean existsByRif(Rif rif) {
        return springDataRepository.existsByRif(rif.getValue());
    }

    @Override
    public boolean existsByRifAndIdNot(Rif rif, CustomerId id) {
        return springDataRepository.existsByRifAndIdNot(rif.getValue(), id.getValue());
    }

    @Override
    public List<Customer> findAll() {
        return springDataRepository.findAll().stream()
            .map(CustomerEntity::toDomain)
            .toList();
    }

    @Override
    public PageResponse<Customer> findAllWithFilters(String search, String rif, String type, Boolean active,
                                                       String state, String municipality,
                                                       String postalCode, Boolean hasActiveWarehouse, int page, int size,
                                                       String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "legalName";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<CustomerEntity> result = springDataRepository.findAllWithFilters(
            search, rif, type, active, state, municipality, postalCode, hasActiveWarehouse, pageable);

        Page<Customer> mapped = result.map(CustomerEntity::toDomain);
        return PageResponseMapper.from(mapped);
    }

    @Override
    public boolean hasAssociatedRecords(CustomerId id) {
        return !findIdsWithAssociatedRecords(Set.of(id.getValue())).isEmpty();
    }

    @Override
    public Set<UUID> findIdsWithAssociatedRecords(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        return springDataRepository.findIdsWithAssociatedRecords(ids);
    }

    @Override
    public void delete(CustomerId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(CustomerId id) {
        return springDataRepository.existsById(id.getValue());
    }

    @Override
    public boolean isActive(CustomerId id) {
        return springDataRepository.isActiveById(id.getValue()).orElse(false);
    }

    @Override
    public long getNextCode() {
        return springDataRepository.findMaxCode().orElse(0L) + 1;
    }
}
