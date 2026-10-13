package com.cashly.cashly_api.suppliers.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
    name = "supplier_addresses",
    indexes = {
        @Index(name = "idx_supplier_addresses_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_supplier_addresses_billing", columnList = "billing")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupplierAddressEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "supplier_id", columnDefinition = "BINARY(16)", nullable = false, insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID supplierId;

    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "billing", nullable = false)
    private boolean billing;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "municipality", length = 100)
    private String municipality;

    @Column(name = "postal_code", length = 10)
    private String postalCode;
}
