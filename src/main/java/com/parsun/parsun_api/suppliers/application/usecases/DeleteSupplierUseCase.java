package com.parsun.parsun_api.suppliers.application.usecases;

import com.parsun.parsun_api.suppliers.application.exceptions.SupplierNotFoundException;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.valueobjects.SupplierId;
import org.springframework.transaction.annotation.Transactional;

public class DeleteSupplierUseCase {

    private final SupplierRepository supplierRepository;

    public DeleteSupplierUseCase(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public void execute(String id) {
        SupplierId supplierId = SupplierId.from(id);
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.delete(supplierId);
    }
}
