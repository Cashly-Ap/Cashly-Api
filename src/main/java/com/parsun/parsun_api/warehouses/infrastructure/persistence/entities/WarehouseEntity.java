package com.parsun.parsun_api.warehouses.infrastructure.persistence.entities;

import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "warehouses",
    indexes = {
        @Index(name = "idx_warehouse_code", columnList = "code"),
        @Index(name = "idx_warehouse_active", columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WarehouseEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "municipality", length = 100)
    private String municipality;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static WarehouseEntity fromDomain(Warehouse w) {
        WarehouseEntity e = new WarehouseEntity();
        e.id = (w.getId() != null) ? w.getId() : UUID.randomUUID();
        e.code = w.getCode();
        e.name = w.getName();
        e.address = w.getAddress();
        e.state = w.getState();
        e.municipality = w.getMunicipality();
        e.postalCode = w.getPostalCode();
        e.active = w.isActive();
        e.createdAt = w.getCreatedAt();
        e.updatedAt = w.getUpdatedAt();
        return e;
    }

    public void updateFromDomain(Warehouse w) {
        this.name = w.getName();
        this.address = w.getAddress();
        this.state = w.getState();
        this.municipality = w.getMunicipality();
        this.postalCode = w.getPostalCode();
        this.active = w.isActive();
        this.updatedAt = w.getUpdatedAt();
    }

    public Warehouse toDomain() {
        return Warehouse.reconstitute(id, code, name, address, state, municipality,
                postalCode, active, createdAt, updatedAt);
    }
}
