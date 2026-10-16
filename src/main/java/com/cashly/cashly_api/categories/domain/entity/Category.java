package com.cashly.cashly_api.categories.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Category {

    private final UUID id;
    private String name;
    private String description;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Category(UUID id, String name, String description, boolean active,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.description = validateDescription(description);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Category create(String name, String description) {
        LocalDateTime now = LocalDateTime.now();
        return new Category(null, name, description, true, now, now);
    }

    public static Category reconstitute(UUID id, String name, String description, boolean active,
                                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Category(id, name, description, active, createdAt, updatedAt);
    }

    public void update(String name, String description, Boolean active) {
        validateName(name);
        this.name = name;
        this.description = validateDescription(description);
        if (active != null) this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be null or blank");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("Category name must be at least 2 characters");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Category name cannot exceed 100 characters");
        }
    }

    private String validateDescription(String description) {
        if (description != null && description.length() > 300) {
            throw new IllegalArgumentException("Category description cannot exceed 300 characters");
        }
        return description;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category that = (Category) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "', active=" + active + "}";
    }
}
