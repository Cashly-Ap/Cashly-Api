package com.cashly.cashly_api.catalog.price_types.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PriceType {

    private final UUID id;
    private String name;
    private String description;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PriceType(UUID id, String name, String description, boolean active,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateName(name);
        this.id = id;
        this.name = name.trim();
        this.description = description;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PriceType create(String name, String description) {
        LocalDateTime now = LocalDateTime.now();
        return new PriceType(null, name, description, true, now, now);
    }

    public static PriceType reconstitute(UUID id, String name,
            String description, boolean active, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new PriceType(id, name, description, active, createdAt,
                updatedAt);
    }

    public void update(String name, String description, Boolean active) {
        validateName(name);
        this.name = name.trim();
        this.description = description;
        if (active != null)
            this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Price type name cannot be null or blank");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Price type name cannot exceed 100 characters");
        }
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public boolean isActive() {
        return active;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PriceType that = (PriceType) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
