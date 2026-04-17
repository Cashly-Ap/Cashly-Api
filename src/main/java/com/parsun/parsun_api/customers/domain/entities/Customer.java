package com.parsun.parsun_api.customers.domain.entities;

import com.parsun.parsun_api.customers.domain.valueobjects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Customer {
    private final CustomerId id;
    private final CustomerCode code;
    private LegalName legalName;
    private CustomerType type;
    private Rif rif;
    private CustomerAlias alias;
    private CreditDays creditDays;
    private CreditLimit creditLimit;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CustomerEmail> emails;
    private List<CustomerPhone> phones;
    private List<CustomerAddress> addresses;

    private Customer(CustomerId id, CustomerCode code, LegalName legalName, CustomerType type,
                     Rif rif, CustomerAlias alias, CreditDays creditDays, CreditLimit creditLimit,
                     boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
                     List<CustomerEmail> emails, List<CustomerPhone> phones, List<CustomerAddress> addresses) {
        this.id = id;
        this.code = code;
        this.legalName = legalName;
        this.type = type;
        this.rif = rif;
        this.alias = alias;
        this.creditDays = creditDays;
        this.creditLimit = creditLimit;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.emails = new ArrayList<>(emails != null ? emails : List.of());
        this.phones = new ArrayList<>(phones != null ? phones : List.of());
        this.addresses = new ArrayList<>(addresses != null ? addresses : List.of());
    }

    public static Customer create(CustomerCode code, LegalName legalName, CustomerType type,
                                   Rif rif, CustomerAlias alias, CreditDays creditDays,
                                   CreditLimit creditLimit) {
        LocalDateTime now = LocalDateTime.now();
        return new Customer(
            CustomerId.generate(), code, legalName, type, rif, alias,
            creditDays, creditLimit, true, now, now,
            new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        );
    }

    public static Customer reconstitute(CustomerId id, CustomerCode code, LegalName legalName,
                                         CustomerType type, Rif rif, CustomerAlias alias,
                                         CreditDays creditDays, CreditLimit creditLimit,
                                         boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
                                         List<CustomerEmail> emails, List<CustomerPhone> phones,
                                         List<CustomerAddress> addresses) {
        return new Customer(id, code, legalName, type, rif, alias, creditDays, creditLimit,
            active, createdAt, updatedAt, emails, phones, addresses);
    }

    public void updateLegalName(LegalName legalName) {
        if (legalName == null) throw new IllegalArgumentException("Legal name cannot be null");
        this.legalName = legalName;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRif(Rif rif) {
        if (rif == null) throw new IllegalArgumentException("RIF cannot be null");
        this.rif = rif;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAlias(CustomerAlias alias) {
        if (alias == null) throw new IllegalArgumentException("Alias cannot be null");
        this.alias = alias;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCreditDays(CreditDays creditDays) {
        if (creditDays == null) throw new IllegalArgumentException("Credit days cannot be null");
        this.creditDays = creditDays;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCreditLimit(CreditLimit creditLimit) {
        if (creditLimit == null) throw new IllegalArgumentException("Credit limit cannot be null");
        this.creditLimit = creditLimit;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateType(CustomerType type) {
        if (type == null) throw new IllegalArgumentException("Customer type cannot be null");
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public void addEmail(EmailAddress emailAddress) {
        CustomerEmail customerEmail = CustomerEmail.create(this.id, emailAddress);
        this.emails.add(customerEmail);
        this.updatedAt = LocalDateTime.now();
    }

    public void addPhone(PhoneNumber phoneNumber) {
        CustomerPhone customerPhone = CustomerPhone.create(this.id, phoneNumber);
        this.phones.add(customerPhone);
        this.updatedAt = LocalDateTime.now();
    }

    public void addAddress(String address, boolean billing, boolean active) {
        if (billing) {
            this.addresses.forEach(CustomerAddress::unmarkAsBilling);
        }
        CustomerAddress customerAddress = CustomerAddress.create(this.id, address, billing, active);
        this.addresses.add(customerAddress);
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

    public CustomerId getId() { return id; }
    public CustomerCode getCode() { return code; }
    public LegalName getLegalName() { return legalName; }
    public CustomerType getType() { return type; }
    public Rif getRif() { return rif; }
    public CustomerAlias getAlias() { return alias; }
    public CreditDays getCreditDays() { return creditDays; }
    public CreditLimit getCreditLimit() { return creditLimit; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<CustomerEmail> getEmails() { return Collections.unmodifiableList(emails); }
    public List<CustomerPhone> getPhones() { return Collections.unmodifiableList(phones); }
    public List<CustomerAddress> getAddresses() { return Collections.unmodifiableList(addresses); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer that = (Customer) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", code=" + code + ", alias=" + alias + ", active=" + active + "}";
    }
}
