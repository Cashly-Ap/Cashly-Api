package com.parsun.parsun_api.customers.infrastructure.persistence.entities;

import com.parsun.parsun_api.customers.domain.entities.*;
import com.parsun.parsun_api.customers.domain.valueobjects.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customers_code", columnList = "code"),
        @Index(name = "idx_customers_rif", columnList = "rif"),
        @Index(name = "idx_customers_active", columnList = "active"),
        @Index(name = "idx_customers_type", columnList = "type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private Long code;

    @Column(name = "legal_name", length = 200, nullable = false)
    private String legalName;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "rif", length = 50, nullable = false)
    private String rif;

    @Column(name = "alias", length = 100, nullable = false)
    private String alias;

    @Column(name = "credit_days", nullable = false)
    private int creditDays;

    @Column(name = "credit_limit", precision = 15, scale = 2, nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "portal_access", nullable = false)
    private boolean portalAccess;

    @Column(name = "portal_password", length = 255)
    private String portalPassword;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "price_type_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID priceTypeId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private List<CustomerEmailEntity> emails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private List<CustomerPhoneEntity> phones = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private List<CustomerAddressEntity> addresses = new ArrayList<>();

    public static CustomerEntity fromDomain(Customer customer) {
        CustomerEntity entity = new CustomerEntity();
        entity.id = customer.getId().getValue();
        entity.code = customer.getCode().getValue();
        entity.legalName = customer.getLegalName().getValue();
        entity.type = customer.getType().name();
        entity.rif = customer.getRif().getValue();
        entity.alias = customer.getAlias().getValue();
        entity.creditDays = customer.getCreditDays().getValue();
        entity.creditLimit = customer.getCreditLimit().getValue();
        entity.active = customer.isActive();
        entity.createdAt = customer.getCreatedAt();
        entity.updatedAt = customer.getUpdatedAt();
        entity.portalAccess = customer.isPortalAccess();
        entity.portalPassword = customer.getPortalPassword();
        entity.notes = customer.getNotes();
        entity.priceTypeId = customer.getPriceTypeId();

        entity.emails = customer.getEmails().stream().map(e -> {
            CustomerEmailEntity emailEntity = new CustomerEmailEntity();
            emailEntity.setId(e.getId().getValue());
            emailEntity.setCustomerId(customer.getId().getValue());
            emailEntity.setEmail(e.getEmail().getValue());
            return emailEntity;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.phones = customer.getPhones().stream().map(p -> {
            CustomerPhoneEntity phoneEntity = new CustomerPhoneEntity();
            phoneEntity.setId(p.getId().getValue());
            phoneEntity.setCustomerId(customer.getId().getValue());
            phoneEntity.setPhone(p.getPhone().getValue());
            return phoneEntity;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.addresses = customer.getAddresses().stream().map(a -> {
            CustomerAddressEntity addressEntity = new CustomerAddressEntity();
            addressEntity.setId(a.getId().getValue());
            addressEntity.setCustomerId(customer.getId().getValue());
            addressEntity.setAddress(a.getAddress());
            addressEntity.setBilling(a.isBilling());
            addressEntity.setActive(a.isActive());
            addressEntity.setState(a.getState());
            addressEntity.setMunicipality(a.getMunicipality());
            addressEntity.setPostalCode(a.getPostalCode());
            return addressEntity;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        return entity;
    }

    public void updateFromDomain(Customer customer) {
        this.legalName = customer.getLegalName().getValue();
        this.type = customer.getType().name();
        this.rif = customer.getRif().getValue();
        this.alias = customer.getAlias().getValue();
        this.creditDays = customer.getCreditDays().getValue();
        this.creditLimit = customer.getCreditLimit().getValue();
        this.active = customer.isActive();
        this.updatedAt = customer.getUpdatedAt();
        this.portalAccess = customer.isPortalAccess();
        this.portalPassword = customer.getPortalPassword();
        this.notes = customer.getNotes();
        this.priceTypeId = customer.getPriceTypeId();

        this.emails.clear();
        customer.getEmails().forEach(e -> {
            CustomerEmailEntity emailEntity = new CustomerEmailEntity();
            emailEntity.setId(e.getId().getValue());
            emailEntity.setCustomerId(this.id);
            emailEntity.setEmail(e.getEmail().getValue());
            this.emails.add(emailEntity);
        });

        this.phones.clear();
        customer.getPhones().forEach(p -> {
            CustomerPhoneEntity phoneEntity = new CustomerPhoneEntity();
            phoneEntity.setId(p.getId().getValue());
            phoneEntity.setCustomerId(this.id);
            phoneEntity.setPhone(p.getPhone().getValue());
            this.phones.add(phoneEntity);
        });

        this.addresses.clear();
        customer.getAddresses().forEach(a -> {
            CustomerAddressEntity addressEntity = new CustomerAddressEntity();
            addressEntity.setId(a.getId().getValue());
            addressEntity.setCustomerId(this.id);
            addressEntity.setAddress(a.getAddress());
            addressEntity.setBilling(a.isBilling());
            addressEntity.setActive(a.isActive());
            addressEntity.setState(a.getState());
            addressEntity.setMunicipality(a.getMunicipality());
            addressEntity.setPostalCode(a.getPostalCode());
            this.addresses.add(addressEntity);
        });
    }

    public Customer toDomain() {
        List<CustomerEmail> domainEmails = this.emails.stream()
            .map(e -> new CustomerEmail(
                new EmailId(e.getId()),
                new CustomerId(e.getCustomerId()),
                new EmailAddress(e.getEmail())
            ))
            .toList();

        List<CustomerPhone> domainPhones = this.phones.stream()
            .map(p -> new CustomerPhone(
                new PhoneId(p.getId()),
                new CustomerId(p.getCustomerId()),
                new PhoneNumber(p.getPhone())
            ))
            .toList();

        List<CustomerAddress> domainAddresses = this.addresses.stream()
            .map(a -> new CustomerAddress(
                new AddressId(a.getId()),
                new CustomerId(a.getCustomerId()),
                a.getAddress(),
                a.isBilling(),
                a.isActive(),
                a.getState(),
                a.getMunicipality(),
                a.getPostalCode()
            ))
            .toList();

        return Customer.reconstitute(
            new CustomerId(this.id),
            new CustomerCode(this.code),
            new LegalName(this.legalName),
            CustomerType.valueOf(this.type),
            new Rif(this.rif),
            new CustomerAlias(this.alias),
            new CreditDays(this.creditDays),
            new CreditLimit(this.creditLimit),
            this.active,
            this.createdAt,
            this.updatedAt,
            domainEmails,
            domainPhones,
            domainAddresses,
            this.portalAccess,
            this.portalPassword,
            this.notes,
            this.priceTypeId
        );
    }
}
