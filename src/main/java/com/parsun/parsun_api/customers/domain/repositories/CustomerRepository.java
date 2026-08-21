package com.parsun.parsun_api.customers.domain.repositories;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.customers.domain.valueobjects.Rif;
import com.parsun.parsun_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId id);
    Optional<Customer> findByRif(Rif rif);
    Optional<Customer> findActivePortalCustomerByRif(Rif rif);
    boolean existsByRif(Rif rif);
    boolean existsByRifAndIdNot(Rif rif, CustomerId id);
    List<Customer> findAll();
    boolean hasAssociatedRecords(CustomerId id);
    Set<UUID> findIdsWithAssociatedRecords(Set<UUID> ids);
    PageResponse<Customer> findAllWithFilters(String search, String rif, String type, Boolean active, Boolean portalAccess, String state, String municipality, String postalCode, Boolean hasActiveWarehouse, int page, int size, String orderBy, String orderDir);
    void delete(CustomerId id);
    boolean existsById(CustomerId id);
    boolean isActive(CustomerId id);
    boolean isActivePortalAccess(CustomerId id);
    long getNextCode();
}
