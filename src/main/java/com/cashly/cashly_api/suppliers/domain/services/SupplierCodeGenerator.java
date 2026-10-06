package com.cashly.cashly_api.suppliers.domain.services;

import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierCode;

public interface SupplierCodeGenerator {
    SupplierCode generateNext();
}
