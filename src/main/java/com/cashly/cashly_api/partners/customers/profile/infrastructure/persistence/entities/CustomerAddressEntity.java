package com.cashly.cashly_api.partners.customers.profile.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
    name = "customer_addresses",
    indexes = {
        @Index(name = "idx_customer_addresses_customer_id", columnList = "customer_id"),
        @Index(name = "idx_customer_addresses_billing", columnList = "billing")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerAddressEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)", nullable = false, insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID customerId;

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
