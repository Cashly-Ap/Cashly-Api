package com.parsun.parsun_api.suppliers.domain.repositories;

import com.parsun.parsun_api.suppliers.domain.entities.Supplier;
import com.parsun.parsun_api.suppliers.domain.valueobjects.SupplierId;
import com.parsun.parsun_api.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(SupplierId id);
    List<Supplier> findAll();
    PageResponse<Supplier> findAllWithFilters(String search, String type, Boolean active, String state, int page, int size, String orderBy, String orderDir);
    void delete(SupplierId id);
    boolean existsById(SupplierId id);
    long getNextCode();
}
