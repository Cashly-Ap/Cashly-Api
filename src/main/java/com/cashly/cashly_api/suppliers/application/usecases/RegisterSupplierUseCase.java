package com.cashly.cashly_api.suppliers.application.usecases;

import com.cashly.cashly_api.suppliers.application.dto.request.CreateSupplierRequest;
import com.cashly.cashly_api.suppliers.application.dto.response.SupplierCreateResponse;
import com.cashly.cashly_api.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.suppliers.domain.entities.Supplier;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.suppliers.domain.services.SupplierCodeGenerator;
import com.cashly.cashly_api.suppliers.domain.valueobjects.*;
import org.springframework.transaction.annotation.Transactional;

public class RegisterSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierCodeGenerator supplierCodeGenerator;
    private final SupplierMapper supplierMapper;

    public RegisterSupplierUseCase(SupplierRepository supplierRepository,
                                    SupplierCodeGenerator supplierCodeGenerator,
                                    SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierCodeGenerator = supplierCodeGenerator;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public SupplierCreateResponse execute(CreateSupplierRequest request) {
        SupplierCode code = supplierCodeGenerator.generateNext();

        Supplier supplier = Supplier.create(
            code,
            new LegalName(request.legalName()),
            SupplierType.valueOf(request.type()),
            new Rif(request.rif()),
            new SupplierAlias(request.alias())
        );

        supplier.updateNotes(request.notes());

        request.emails().forEach(e -> supplier.addEmail(new EmailAddress(e.email())));
        request.phones().forEach(p -> supplier.addPhone(new PhoneNumber(p.phone())));
        request.addresses().forEach(a -> supplier.addAddress(
            a.address(), a.billing(), a.active(),
            a.state(), a.municipality(), a.postalCode()
        ));

        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toCreateResponse(saved);
    }
}
