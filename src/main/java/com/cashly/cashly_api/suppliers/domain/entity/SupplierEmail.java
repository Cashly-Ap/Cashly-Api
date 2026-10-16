package com.cashly.cashly_api.suppliers.domain.entity;

import com.cashly.cashly_api.suppliers.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.suppliers.domain.valueobjects.EmailId;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;

import java.util.Objects;

public class SupplierEmail {
    private final EmailId id;
    private final SupplierId supplierId;
    private EmailAddress email;

    public SupplierEmail(EmailId id, SupplierId supplierId, EmailAddress email) {
        if (id == null) throw new IllegalArgumentException("Email ID cannot be null");
        if (supplierId == null) throw new IllegalArgumentException("Supplier ID cannot be null");
        if (email == null) throw new IllegalArgumentException("Email address cannot be null");

        this.id = id;
        this.supplierId = supplierId;
        this.email = email;
    }

    public static SupplierEmail create(SupplierId supplierId, EmailAddress email) {
        return new SupplierEmail(EmailId.generate(), supplierId, email);
    }

    public EmailId getId() { return id; }
    public SupplierId getSupplierId() { return supplierId; }
    public EmailAddress getEmail() { return email; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SupplierEmail that = (SupplierEmail) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
