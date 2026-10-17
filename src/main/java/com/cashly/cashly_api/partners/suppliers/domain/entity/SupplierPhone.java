package com.cashly.cashly_api.partners.suppliers.domain.entity;

import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.PhoneId;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;

import java.util.Objects;

public class SupplierPhone {
    private final PhoneId id;
    private final SupplierId supplierId;
    private PhoneNumber phone;

    public SupplierPhone(PhoneId id, SupplierId supplierId, PhoneNumber phone) {
        if (id == null) throw new IllegalArgumentException("Phone ID cannot be null");
        if (supplierId == null) throw new IllegalArgumentException("Supplier ID cannot be null");
        if (phone == null) throw new IllegalArgumentException("Phone number cannot be null");

        this.id = id;
        this.supplierId = supplierId;
        this.phone = phone;
    }

    public static SupplierPhone create(SupplierId supplierId, PhoneNumber phone) {
        return new SupplierPhone(PhoneId.generate(), supplierId, phone);
    }

    public PhoneId getId() { return id; }
    public SupplierId getSupplierId() { return supplierId; }
    public PhoneNumber getPhone() { return phone; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupplierPhone that = (SupplierPhone) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
