package com.parsun.parsun_api.suppliers.application.usecases;

import com.parsun.parsun_api.suppliers.application.dto.response.SupplierDetailResponse;
import com.parsun.parsun_api.suppliers.application.exceptions.SupplierNotFoundException;
import com.parsun.parsun_api.suppliers.application.mappers.SupplierMapper;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.valueobjects.SupplierId;
import org.springframework.transaction.annotation.Transactional;

public class GetSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public GetSupplierUseCase(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public SupplierDetailResponse execute(String id) {
        SupplierId supplierId = SupplierId.from(id);
        return supplierRepository.findById(supplierId)
            .map(supplierMapper::toDetailResponse)
            .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));
    }
}
