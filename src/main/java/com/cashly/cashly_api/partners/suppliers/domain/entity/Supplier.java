package com.cashly.cashly_api.partners.suppliers.domain.entity;

import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.Rif;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierAlias;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierCode;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.partners.suppliers.domain.valueobjects.SupplierType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Supplier {
    private final SupplierId id;
    private final SupplierCode code;
    private LegalName legalName;
    private SupplierType type;
    private Rif rif;
    private SupplierAlias alias;
    private CreditDays creditDays;
    private CreditLimit creditLimit;
    private String notes;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SupplierEmail> emails;
    private List<SupplierPhone> phones;
    private List<SupplierAddress> addresses;

    private Supplier(SupplierId id, SupplierCode code, LegalName legalName,
            SupplierType type, Rif rif, SupplierAlias alias,
            CreditDays creditDays, CreditLimit creditLimit, String notes,
            boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
            List<SupplierEmail> emails, List<SupplierPhone> phones,
            List<SupplierAddress> addresses) {
        this.id = id;
        this.code = code;
        this.legalName = legalName;
        this.type = type;
        this.rif = rif;
        this.alias = alias;
        this.creditDays = creditDays;
        this.creditLimit = creditLimit;
        this.notes = notes;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.emails = new ArrayList<>(emails != null ? emails : List.of());
        this.phones = new ArrayList<>(phones != null ? phones : List.of());
        this.addresses = new ArrayList<>(
                addresses != null ? addresses : List.of());
    }

    public static Supplier create(SupplierCode code, LegalName legalName,
            SupplierType type, Rif rif, SupplierAlias alias) {
        LocalDateTime now = LocalDateTime.now();
        return new Supplier(SupplierId.generate(), code, legalName, type, rif,
                alias, new CreditDays(0),
                new CreditLimit(java.math.BigDecimal.ZERO), null, true, now,
                now, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>());
    }

    public static Supplier reconstitute(SupplierId id, SupplierCode code,
            LegalName legalName, SupplierType type, Rif rif,
            SupplierAlias alias, CreditDays creditDays, CreditLimit creditLimit,
            String notes, boolean active, LocalDateTime createdAt,
            LocalDateTime updatedAt, List<SupplierEmail> emails,
            List<SupplierPhone> phones, List<SupplierAddress> addresses) {
        return new Supplier(id, code, legalName, type, rif, alias, creditDays,
                creditLimit, notes, active, createdAt, updatedAt, emails,
                phones, addresses);
    }

    public static Supplier reconstitute(SupplierId id, SupplierCode code,
            LegalName legalName, SupplierType type, Rif rif,
            SupplierAlias alias, String notes, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            List<SupplierEmail> emails, List<SupplierPhone> phones,
            List<SupplierAddress> addresses) {
        return reconstitute(id, code, legalName, type, rif, alias,
                new CreditDays(0),
                new CreditLimit(java.math.BigDecimal.ZERO), notes, active,
                createdAt, updatedAt, emails, phones, addresses);
    }

    public void updateCreditDays(CreditDays creditDays) {
        if (creditDays == null)
            throw new IllegalArgumentException("Credit days cannot be null");
        this.creditDays = creditDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCreditLimit(CreditLimit creditLimit) {
        if (creditLimit == null)
            throw new IllegalArgumentException("Credit limit cannot be null");
        this.creditLimit = creditLimit;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateLegalName(LegalName legalName) {
        if (legalName == null)
            throw new IllegalArgumentException("Legal name cannot be null");
        this.legalName = legalName;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRif(Rif rif) {
        if (rif == null)
            throw new IllegalArgumentException("RIF cannot be null");
        this.rif = rif;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAlias(SupplierAlias alias) {
        if (alias == null)
            throw new IllegalArgumentException("Alias cannot be null");
        this.alias = alias;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateType(SupplierType type) {
        if (type == null)
            throw new IllegalArgumentException("Supplier type cannot be null");
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void addEmail(EmailAddress emailAddress) {
        SupplierEmail supplierEmail = SupplierEmail.create(this.id,
                emailAddress);
        this.emails.add(supplierEmail);
        this.updatedAt = LocalDateTime.now();
    }

    public void addPhone(PhoneNumber phoneNumber) {
        SupplierPhone supplierPhone = SupplierPhone.create(this.id,
                phoneNumber);
        this.phones.add(supplierPhone);
        this.updatedAt = LocalDateTime.now();
    }

    public void addAddress(String address, boolean billing, boolean active,
            String state, String municipality, String postalCode) {
        if (billing) {
            this.addresses.forEach(SupplierAddress::unmarkAsBilling);
        }
        SupplierAddress supplierAddress = SupplierAddress.create(this.id,
                address, billing, active, state, municipality, postalCode);
        this.addresses.add(supplierAddress);
        this.updatedAt = LocalDateTime.now();
    }

    public void clearEmails() {
        this.emails.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearPhones() {
        this.phones.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public void clearAddresses() {
        this.addresses.clear();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public SupplierId getId() {
        return id;
    }
    public SupplierCode getCode() {
        return code;
    }
    public LegalName getLegalName() {
        return legalName;
    }
    public SupplierType getType() {
        return type;
    }
    public Rif getRif() {
        return rif;
    }
    public CreditDays getCreditDays() {
        return creditDays;
    }

    public CreditLimit getCreditLimit() {
        return creditLimit;
    }

    public SupplierAlias getAlias() {
        return alias;
    }
    public String getNotes() {
        return notes;
    }
    public boolean isActive() {
        return active;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public List<SupplierEmail> getEmails() {
        return Collections.unmodifiableList(emails);
    }
    public List<SupplierPhone> getPhones() {
        return Collections.unmodifiableList(phones);
    }
    public List<SupplierAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Supplier that = (Supplier) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Supplier{id=" + id + ", code=" + code + ", alias=" + alias
                + ", active=" + active + "}";
    }
}
