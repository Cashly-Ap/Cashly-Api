package com.parsun.parsun_api.suppliers.domain.services;

import com.parsun.parsun_api.suppliers.domain.valueobjects.SupplierCode;

public interface SupplierCodeGenerator {
    SupplierCode generateNext();
}
