package com.cashly.cashly_api.suppliers.application.usecases;

import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.suppliers.application.exceptions.SupplierHasAssociatedRecordsException;
import com.cashly.cashly_api.suppliers.application.exceptions.SupplierNotFoundException;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;

    public DeleteSupplierUseCase(SupplierRepository supplierRepository,
                                  PurchaseRepository purchaseRepository) {
        this.supplierRepository = supplierRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public void execute(String id) {
        SupplierId supplierId = SupplierId.from(id);
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + id);
        }
        if (purchaseRepository.existsBySupplierId(UUID.fromString(supplierId.toString()))) {
            throw new SupplierHasAssociatedRecordsException(id);
        }
        supplierRepository.delete(supplierId);
    }
}
