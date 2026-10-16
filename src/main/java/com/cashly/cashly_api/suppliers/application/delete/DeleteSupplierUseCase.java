package com.cashly.cashly_api.suppliers.application.delete;

import com.cashly.cashly_api.suppliers.application.ports.out.PurchaseUsageGateway;
import com.cashly.cashly_api.suppliers.application.exceptions.SupplierHasAssociatedRecordsException;
import com.cashly.cashly_api.suppliers.application.exceptions.SupplierNotFoundException;
import com.cashly.cashly_api.suppliers.domain.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final PurchaseUsageGateway purchaseUsage;

    public DeleteSupplierUseCase(SupplierRepository supplierRepository,
                                  PurchaseUsageGateway purchaseUsage) {
        this.supplierRepository = supplierRepository;
        this.purchaseUsage = purchaseUsage;
    }

    @Transactional
    public void execute(String id) {
        SupplierId supplierId = SupplierId.from(id);
        if (!supplierRepository.existsById(supplierId)) {
            throw new SupplierNotFoundException("Supplier not found with id: " + id);
        }
        if (purchaseUsage.hasPurchases(UUID.fromString(supplierId.toString()))) {
            throw new SupplierHasAssociatedRecordsException(id);
        }
        supplierRepository.delete(supplierId);
    }
}
