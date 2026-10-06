package com.cashly.cashly_api.suppliers.infrastructure.persistence.entities;

import com.cashly.cashly_api.suppliers.domain.entities.*;
import com.cashly.cashly_api.suppliers.domain.valueobjects.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "suppliers",
    indexes = {
        @Index(name = "idx_suppliers_code", columnList = "code"),
        @Index(name = "idx_suppliers_rif", columnList = "rif"),
        @Index(name = "idx_suppliers_active", columnList = "active"),
        @Index(name = "idx_suppliers_type", columnList = "type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupplierEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private Long code;

    @Column(name = "legal_name", length = 200, nullable = false)
    private String legalName;

    @Column(name = "alias", length = 100, nullable = false)
    private String alias;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "rif", length = 50, nullable = false)
    private String rif;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private List<SupplierEmailEntity> emails = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private List<SupplierPhoneEntity> phones = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private List<SupplierAddressEntity> addresses = new ArrayList<>();

    public static SupplierEntity fromDomain(Supplier supplier) {
        SupplierEntity entity = new SupplierEntity();
        entity.id = supplier.getId().getValue();
        entity.code = supplier.getCode().getValue();
        entity.legalName = supplier.getLegalName().getValue();
        entity.alias = supplier.getAlias().getValue();
        entity.type = supplier.getType().name();
        entity.rif = supplier.getRif().getValue();
        entity.notes = supplier.getNotes();
        entity.active = supplier.isActive();
        entity.createdAt = supplier.getCreatedAt();
        entity.updatedAt = supplier.getUpdatedAt();

        entity.emails = supplier.getEmails().stream().map(e -> {
            SupplierEmailEntity emailEntity = new SupplierEmailEntity();
            emailEntity.setId(e.getId().getValue());
            emailEntity.setSupplierId(supplier.getId().getValue());
            emailEntity.setEmail(e.getEmail().getValue());
            return emailEntity;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.phones = supplier.getPhones().stream().map(p -> {
            SupplierPhoneEntity phoneEntity = new SupplierPhoneEntity();
            phoneEntity.setId(p.getId().getValue());
            phoneEntity.setSupplierId(supplier.getId().getValue());
            phoneEntity.setPhone(p.getPhone().getValue());
            return phoneEntity;
        }).collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        entity.addresses = supplier.getAddresses().stream().map(a -> {
            SupplierAddressEntity addressEntity = new SupplierAddressEntity();
            addressEntity.setId(a.getId().getValue());
            addressEntity.setSupplierId(supplier.getId().getValue());
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

    public void updateFromDomain(Supplier supplier) {
        this.legalName = supplier.getLegalName().getValue();
        this.alias = supplier.getAlias().getValue();
        this.type = supplier.getType().name();
        this.rif = supplier.getRif().getValue();
        this.notes = supplier.getNotes();
        this.active = supplier.isActive();
        this.updatedAt = supplier.getUpdatedAt();

        this.emails.clear();
        supplier.getEmails().forEach(e -> {
            SupplierEmailEntity emailEntity = new SupplierEmailEntity();
            emailEntity.setId(e.getId().getValue());
            emailEntity.setSupplierId(this.id);
            emailEntity.setEmail(e.getEmail().getValue());
            this.emails.add(emailEntity);
        });

        this.phones.clear();
        supplier.getPhones().forEach(p -> {
            SupplierPhoneEntity phoneEntity = new SupplierPhoneEntity();
            phoneEntity.setId(p.getId().getValue());
            phoneEntity.setSupplierId(this.id);
            phoneEntity.setPhone(p.getPhone().getValue());
            this.phones.add(phoneEntity);
        });

        this.addresses.clear();
        supplier.getAddresses().forEach(a -> {
            SupplierAddressEntity addressEntity = new SupplierAddressEntity();
            addressEntity.setId(a.getId().getValue());
            addressEntity.setSupplierId(this.id);
            addressEntity.setAddress(a.getAddress());
            addressEntity.setBilling(a.isBilling());
            addressEntity.setActive(a.isActive());
            addressEntity.setState(a.getState());
            addressEntity.setMunicipality(a.getMunicipality());
            addressEntity.setPostalCode(a.getPostalCode());
            this.addresses.add(addressEntity);
        });
    }

    public Supplier toDomain() {
        List<SupplierEmail> domainEmails = this.emails.stream()
            .map(e -> new SupplierEmail(
                new EmailId(e.getId()),
                new SupplierId(e.getSupplierId()),
                new EmailAddress(e.getEmail())
            ))
            .toList();

        List<SupplierPhone> domainPhones = this.phones.stream()
            .map(p -> new SupplierPhone(
                new PhoneId(p.getId()),
                new SupplierId(p.getSupplierId()),
                new PhoneNumber(p.getPhone())
            ))
            .toList();

        List<SupplierAddress> domainAddresses = this.addresses.stream()
            .map(a -> new SupplierAddress(
                new AddressId(a.getId()),
                new SupplierId(a.getSupplierId()),
                a.getAddress(),
                a.isBilling(),
                a.isActive(),
                a.getState(),
                a.getMunicipality(),
                a.getPostalCode()
            ))
            .toList();

        return Supplier.reconstitute(
            new SupplierId(this.id),
            SupplierCode.from(this.code),
            new LegalName(this.legalName),
            SupplierType.valueOf(this.type),
            new Rif(this.rif),
            new SupplierAlias(this.alias),
            this.notes,
            this.active,
            this.createdAt,
            this.updatedAt,
            domainEmails,
            domainPhones,
            domainAddresses
        );
    }
}
