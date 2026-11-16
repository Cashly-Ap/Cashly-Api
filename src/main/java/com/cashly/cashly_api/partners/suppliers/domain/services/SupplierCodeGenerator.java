package com.cashly.cashly_api.partners.suppliers.domain.services;

import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierCode;

public interface SupplierCodeGenerator {
    SupplierCode generateNext();
}
