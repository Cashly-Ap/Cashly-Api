package com.parsun.parsun_api.suppliers.application.mappers;

import com.parsun.parsun_api.suppliers.application.dto.response.*;
import com.parsun.parsun_api.suppliers.domain.entities.Supplier;

public class SupplierMapper {

    public SupplierListResponse toListResponse(Supplier supplier) {
        return toListResponse(supplier, false);
    }

    public SupplierListResponse toListResponse(Supplier supplier, boolean hasPurchases) {
        return new SupplierListResponse(
            supplier.getId().toString(),
            supplier.getCode().getValue(),
            supplier.getLegalName().getValue(),
            supplier.getAlias().getValue(),
            supplier.getType().name(),
            supplier.getRif().getValue(),
            supplier.isActive(),
            supplier.getCreatedAt().toString(),
            hasPurchases
        );
    }

    public SupplierDetailResponse toDetailResponse(Supplier supplier) {
        return new SupplierDetailResponse(
            supplier.getId().toString(),
            supplier.getCode().getValue(),
            supplier.getLegalName().getValue(),
            supplier.getAlias().getValue(),
            supplier.getType().name(),
            supplier.getRif().getValue(),
            supplier.isActive(),
            supplier.getNotes(),
            supplier.getCreatedAt(),
            supplier.getEmails().stream()
                .map(e -> new EmailResponse(
                    e.getId().toString(),
                    e.getEmail().getValue()
                ))
                .toList(),
            supplier.getPhones().stream()
                .map(p -> new PhoneResponse(
                    p.getId().toString(),
                    p.getPhone().getValue()
                ))
                .toList(),
            supplier.getAddresses().stream()
                .map(a -> new AddressResponse(
                    a.getId().toString(),
                    a.getAddress(),
                    a.isBilling(),
                    a.isActive(),
                    a.getState(),
                    a.getMunicipality(),
                    a.getPostalCode()
                ))
                .toList()
        );
    }

    public SupplierCreateResponse toCreateResponse(Supplier supplier) {
        return new SupplierCreateResponse(
            supplier.getId().toString(),
            supplier.getCode().getValue(),
            supplier.getLegalName().getValue(),
            supplier.getAlias().getValue(),
            supplier.getType().name(),
            supplier.getRif().getValue()
        );
    }
}
