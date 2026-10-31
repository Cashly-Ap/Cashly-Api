package com.cashly.cashly_api.partners.suppliers.application.update.use_case;

import com.cashly.cashly_api.partners.suppliers.application.update.request.UpdateSupplierRequest;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierDetailResponse;
import com.cashly.cashly_api.partners.suppliers.application.exceptions.SupplierNotFoundException;
import com.cashly.cashly_api.partners.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.partners.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.Rif;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierAlias;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierType;
import org.springframework.transaction.annotation.Transactional;

public class UpdateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public UpdateSupplierUseCase(SupplierRepository supplierRepository,
            SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional
    public SupplierDetailResponse execute(String id,
            UpdateSupplierRequest request) {
        SupplierId supplierId = SupplierId.from(id);

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Supplier not found with id: " + id));

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

        request.emails()
                .forEach(e -> supplier.addEmail(new EmailAddress(e.email())));
        request.phones()
                .forEach(p -> supplier.addPhone(new PhoneNumber(p.phone())));
        request.addresses()
                .forEach(a -> supplier.addAddress(a.address(), a.billing(),
                        a.active(), a.state(), a.municipality(),
                        a.postalCode()));

        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toDetailResponse(saved);
    }
}
