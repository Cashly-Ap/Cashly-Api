package com.cashly.cashly_api.inventory.warehouses.management.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Warehouse {

    private final UUID id;
    private String code;
    private String name;
    private String address;
    private String state;
    private String municipality;
    private String postalCode;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Warehouse(UUID id, String code, String name, String address, String state,
                      String municipality, String postalCode, boolean active,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        validateCode(code);
        validateName(name);
        this.id = id;
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.address = address;
        this.state = state;
        this.municipality = municipality;
        this.postalCode = postalCode;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Warehouse create(String code, String name, String address, String state,
                                    String municipality, String postalCode) {
        LocalDateTime now = LocalDateTime.now();
        return new Warehouse(null, code, name, address, state, municipality, postalCode, true, now, now);
    }

    public static Warehouse reconstitute(UUID id, String code, String name, String address,
                                          String state, String municipality, String postalCode,
                                          boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Warehouse(id, code, name, address, state, municipality, postalCode, active, createdAt, updatedAt);
    }

    public void update(String name, String address, String state, String municipality, String postalCode, Boolean active) {
        validateName(name);
        this.name = name.trim();
        this.address = address;
        this.state = state;
        this.municipality = municipality;
        this.postalCode = postalCode;
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

    private void validateCode(String code) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Warehouse code cannot be blank");
        if (code.trim().length() > 50) throw new IllegalArgumentException("Warehouse code cannot exceed 50 characters");
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Warehouse name cannot be blank");
        if (name.trim().length() > 100) throw new IllegalArgumentException("Warehouse name cannot exceed 100 characters");
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getState() { return state; }
    public String getMunicipality() { return municipality; }
    public String getPostalCode() { return postalCode; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Warehouse) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
