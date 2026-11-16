package com.cashly.cashly_api.partners.suppliers.infrastructure.services;

import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.services.SupplierCodeGenerator;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierCode;

public class SequentialSupplierCodeGenerator implements SupplierCodeGenerator {

    private final SupplierRepository supplierRepository;

    public SequentialSupplierCodeGenerator(
            SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public SupplierCode generateNext() {
        long nextCode = supplierRepository.getNextCode();
        return SupplierCode.from(nextCode);
    }
}
