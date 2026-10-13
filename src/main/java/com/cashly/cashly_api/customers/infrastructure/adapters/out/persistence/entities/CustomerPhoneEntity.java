package com.cashly.cashly_api.customers.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
    name = "customer_phones",
    indexes = {
        @Index(name = "idx_customer_phones_customer_id", columnList = "customer_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerPhoneEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "customer_id", columnDefinition = "BINARY(16)", nullable = false, insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID customerId;

    @Column(name = "phone", length = 50, nullable = false)
    private String phone;
}
