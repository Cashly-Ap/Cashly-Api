package com.cashly.cashly_api.customers.warehouse_access.infrastructure.persistence.entities;

import com.cashly.cashly_api.customers.warehouse_access.domain.CustomerWarehouseAccess;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "customer_warehouse_access",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customer_warehouse",
                columnNames = {"customer_id", "warehouse_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CustomerWarehouseAccessEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "customer_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID customerId;

    @Column(name = "warehouse_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID warehouseId;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "granted_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID grantedBy;

    public static CustomerWarehouseAccessEntity fromDomain(CustomerWarehouseAccess access) {
        CustomerWarehouseAccessEntity entity = new CustomerWarehouseAccessEntity();
        entity.id = (access.getId() != null) ? access.getId() : UUID.randomUUID();
        entity.customerId = access.getCustomerId();
        entity.warehouseId = access.getWarehouseId();
        entity.grantedAt = access.getGrantedAt();
        entity.grantedBy = access.getGrantedBy();
        return entity;
    }

    public CustomerWarehouseAccess toDomain() {
        return new CustomerWarehouseAccess(id, customerId, warehouseId, grantedAt, grantedBy);
    }
}
