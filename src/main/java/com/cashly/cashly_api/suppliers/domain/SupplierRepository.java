package com.cashly.cashly_api.suppliers.domain;

import com.cashly.cashly_api.suppliers.domain.Supplier;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(SupplierId id);
    List<Supplier> findAllById(Collection<SupplierId> ids);
    List<Supplier> findAll();
    PageResponse<Supplier> findAllWithFilters(String search, String type, Boolean active, String state, int page, int size, String orderBy, String orderDir);
    void delete(SupplierId id);
    boolean existsById(SupplierId id);
    long getNextCode();
}
