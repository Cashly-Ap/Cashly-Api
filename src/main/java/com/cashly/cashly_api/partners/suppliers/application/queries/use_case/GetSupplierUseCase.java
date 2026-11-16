package com.cashly.cashly_api.partners.suppliers.application.queries.use_case;

import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierDetailResponse;
import com.cashly.cashly_api.partners.suppliers.application.exceptions.SupplierNotFoundException;
import com.cashly.cashly_api.partners.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;
import org.springframework.transaction.annotation.Transactional;

public class GetSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public GetSupplierUseCase(SupplierRepository supplierRepository,
            SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public SupplierDetailResponse execute(String id) {
        SupplierId supplierId = SupplierId.from(id);
        return supplierRepository.findById(supplierId)
                .map(supplierMapper::toDetailResponse)
                .orElseThrow(() -> new SupplierNotFoundException(
                        "Supplier not found with id: " + id));
    }
}
