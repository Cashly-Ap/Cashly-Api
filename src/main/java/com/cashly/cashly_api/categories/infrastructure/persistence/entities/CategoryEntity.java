package com.cashly.cashly_api.categories.infrastructure.persistence.entities;

import com.cashly.cashly_api.categories.domain.entity.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "categories",
    indexes = {
        @Index(name = "idx_categories_name", columnList = "name"),
        @Index(name = "idx_categories_active", columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoryEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static CategoryEntity fromDomain(Category category) {
        CategoryEntity entity = new CategoryEntity();
        entity.id = category.getId();
        entity.name = category.getName();
        entity.description = category.getDescription();
        entity.active = category.isActive();
        entity.createdAt = category.getCreatedAt();
        entity.updatedAt = category.getUpdatedAt();
        return entity;
    }

    public void updateFromDomain(Category category) {
        this.name = category.getName();
        this.description = category.getDescription();
        this.active = category.isActive();
        this.updatedAt = category.getUpdatedAt();
    }

    public Category toDomain() {
        return Category.reconstitute(id, name, description, active, createdAt, updatedAt);
    }
}
