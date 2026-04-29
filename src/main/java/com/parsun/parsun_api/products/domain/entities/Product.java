package com.parsun.parsun_api.products.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Product {

    private final UUID id;
    private String code;
    private String name;
    private String description;
    private UUID categoryId;
    private boolean composite;
    private boolean active;
    private String photo;
    private BigDecimal basePrice;
    private BigDecimal purchasePrice;
    private BigDecimal profitMargin;
    private List<ProductComponent> components;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Product(UUID id, String code, String name, String description, UUID categoryId,
                    boolean composite, boolean active, String photo,
                    BigDecimal basePrice, BigDecimal purchasePrice, BigDecimal profitMargin,
                    List<ProductComponent> components, LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateCode(code);
        validateName(name);
        this.id = id;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description;
        this.categoryId = categoryId;
        this.composite = composite;
        this.active = active;
        this.photo = photo;
        this.basePrice = basePrice;
        this.purchasePrice = purchasePrice;
        this.profitMargin = profitMargin;
        this.components = new ArrayList<>(components != null ? components : List.of());
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(String code, String name, String description, UUID categoryId,
                                  boolean composite,
                                  BigDecimal basePrice, BigDecimal purchasePrice, BigDecimal profitMargin) {
        LocalDateTime now = LocalDateTime.now();
        return new Product(null, code, name, description, categoryId, composite,
                true, null, basePrice, purchasePrice, profitMargin, List.of(), now, now);
    }

    public static Product reconstitute(UUID id, String code, String name, String description,
                                        UUID categoryId, boolean composite,
                                        boolean active, String photo,
                                        BigDecimal basePrice, BigDecimal purchasePrice,
                                        BigDecimal profitMargin, List<ProductComponent> components,
                                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Product(id, code, name, description, categoryId, composite,
                active, photo, basePrice, purchasePrice, profitMargin, components, createdAt, updatedAt);
    }

    public void update(String name, String description, UUID categoryId,
                       BigDecimal basePrice, BigDecimal purchasePrice, BigDecimal profitMargin,
                       Boolean active) {
        validateName(name);
        this.name = name.trim();
        this.description = description;
        this.categoryId = categoryId;
        this.basePrice = basePrice;
        this.purchasePrice = purchasePrice;
        this.profitMargin = profitMargin;
        if (active != null) this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public void makeComposite() {
        this.composite = true;
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

    public void setComponents(List<ProductComponent> components) {
        if (!this.composite) {
            throw new IllegalStateException("Cannot set components on a non-composite product");
        }
        this.components = new ArrayList<>(components);
        this.updatedAt = LocalDateTime.now();
    }

    public void assignPhoto(String photoPath) {
        this.photo = photoPath;
        this.updatedAt = LocalDateTime.now();
    }

    public void removePhoto() {
        this.photo = null;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Product code cannot be null or blank");
        }
        if (code.trim().length() > 50) {
            throw new IllegalArgumentException("Product code cannot exceed 50 characters");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (name.trim().length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public UUID getCategoryId() { return categoryId; }
    public boolean isComposite() { return composite; }
    public boolean isActive() { return active; }
    public String getPhoto() { return photo; }
    public BigDecimal getBasePrice() { return basePrice; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public BigDecimal getProfitMargin() { return profitMargin; }
    public List<ProductComponent> getComponents() { return Collections.unmodifiableList(components); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product that = (Product) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
