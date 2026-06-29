package com.parsun.parsun_api.suppliers.application.usecases;

import com.parsun.parsun_api.suppliers.application.dto.request.UpdateSupplierRequest;
import com.parsun.parsun_api.suppliers.application.dto.response.SupplierDetailResponse;
import com.parsun.parsun_api.suppliers.application.exceptions.SupplierNotFoundException;
import com.parsun.parsun_api.suppliers.application.mappers.SupplierMapper;
import com.parsun.parsun_api.suppliers.domain.entities.Supplier;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.suppliers.domain.valueobjects.*;
import org.springframework.transaction.annotation.Transactional;

public class UpdateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public UpdateSupplierUseCase(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public SupplierDetailResponse execute(String id, UpdateSupplierRequest request) {
        SupplierId supplierId = SupplierId.from(id);

        Supplier supplier = supplierRepository.findById(supplierId)
            .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with id: " + id));

        supplier.updateLegalName(new LegalName(request.legalName()));
        supplier.updateType(SupplierType.valueOf(request.type()));
        supplier.updateRif(new Rif(request.rif()));
        supplier.updateAlias(new SupplierAlias(request.alias()));
        supplier.updateNotes(request.notes());

        if (request.active()) {
            supplier.activate();
        } else {
            supplier.deactivate();
        }

        supplier.clearEmails();
        supplier.clearPhones();
        supplier.clearAddresses();

        request.emails().forEach(e -> supplier.addEmail(new EmailAddress(e.email())));
        request.phones().forEach(p -> supplier.addPhone(new PhoneNumber(p.phone())));
        request.addresses().forEach(a -> supplier.addAddress(
            a.address(), a.billing(), a.active(),
            a.state(), a.municipality(), a.postalCode()
        ));

        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toDetailResponse(saved);
    }
}
