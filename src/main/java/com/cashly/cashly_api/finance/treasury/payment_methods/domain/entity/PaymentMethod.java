package com.cashly.cashly_api.finance.treasury.payment_methods.domain.entity;

import com.cashly.cashly_api.finance.treasury.payment_methods.domain.valueobjects.PaymentMethodId;

import java.time.LocalDateTime;
import java.util.Objects;

public class PaymentMethod {

    private final PaymentMethodId id;
    private final String code;
    private String name;
    private String description;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PaymentMethod(PaymentMethodId id, String code, String name, String description,
                          boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentMethod create(String code, String name, String description) {
        Objects.requireNonNull(code, "Code cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return new PaymentMethod(PaymentMethodId.generate(), code, name, description, true, now, now);
    }

    public static PaymentMethod reconstitute(PaymentMethodId id, String code, String name,
                                              String description, boolean isActive,
                                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PaymentMethod(id, code, name, description, isActive, createdAt, updatedAt);
    }

    public void update(String name, String description, Boolean isActive) {
        Objects.requireNonNull(name, "Name cannot be null");
        this.name = name;
        this.description = description;
        if (isActive != null) {
            this.isActive = isActive;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void toggleActive() {
        this.isActive = !this.isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentMethodId getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentMethod that = (PaymentMethod) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PaymentMethod{id=" + id + ", code='" + code + "', name='" + name + "', active=" + isActive + "}";
    }
}
