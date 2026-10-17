package com.cashly.cashly_api.catalog.price_types.infrastructure.persistence.entities;

import com.cashly.cashly_api.catalog.price_types.domain.entity.PriceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_types")
@Getter
@Setter
@NoArgsConstructor
public class PriceTypeEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static PriceTypeEntity fromDomain(PriceType priceType) {
        PriceTypeEntity entity = new PriceTypeEntity();
        entity.id = priceType.getId();
        entity.name = priceType.getName();
        entity.description = priceType.getDescription();
        entity.active = priceType.isActive();
        entity.createdAt = priceType.getCreatedAt();
        entity.updatedAt = priceType.getUpdatedAt();
        return entity;
    }

    public void updateFromDomain(PriceType priceType) {
        this.name = priceType.getName();
        this.description = priceType.getDescription();
        this.active = priceType.isActive();
        this.updatedAt = priceType.getUpdatedAt();
    }

    public PriceType toDomain() {
        return PriceType.reconstitute(id, name, description, active, createdAt, updatedAt);
    }
}
