package com.cashly.cashly_api.partners.suppliers.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "supplier_emails", indexes = {
        @Index(name = "idx_supplier_emails_supplier_id", columnList = "supplier_id")})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SupplierEmailEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "supplier_id", columnDefinition = "BINARY(16)", nullable = false, insertable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID supplierId;

    @Column(name = "email", length = 255, nullable = false)
    private String email;
}
