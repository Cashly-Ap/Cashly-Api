package com.cashly.cashly_api.partners.suppliers.application.register.use_case;

import com.cashly.cashly_api.partners.suppliers.application.register.request.CreateSupplierRequest;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierCreateResponse;
import com.cashly.cashly_api.partners.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.partners.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.services.SupplierCodeGenerator;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.Rif;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierAlias;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierCode;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierType;
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
