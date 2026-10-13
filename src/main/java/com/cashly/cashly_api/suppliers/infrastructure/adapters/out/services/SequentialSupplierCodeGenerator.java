package com.cashly.cashly_api.suppliers.infrastructure.adapters.out.services;

import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.services.SupplierCodeGenerator;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierCode;

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
