package com.parsun.parsun_api.suppliers.infrastructure.services;

import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.services.SupplierCodeGenerator;
import com.parsun.parsun_api.suppliers.domain.valueobjects.SupplierCode;

public class SequentialSupplierCodeGenerator implements SupplierCodeGenerator {

    private final SupplierRepository supplierRepository;

    public SequentialSupplierCodeGenerator(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public SupplierCode generateNext() {
        long nextCode = supplierRepository.getNextCode();
        return SupplierCode.from(nextCode);
    }
}
