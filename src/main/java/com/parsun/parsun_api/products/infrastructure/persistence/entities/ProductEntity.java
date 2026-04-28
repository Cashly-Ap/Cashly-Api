package com.parsun.parsun_api.products.infrastructure.persistence.entities;

import com.parsun.parsun_api.products.domain.entities.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_code", columnList = "code"),
        @Index(name = "idx_product_category", columnList = "category_id"),
        @Index(name = "idx_product_active", columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID categoryId;

    @Column(name = "composite", nullable = false)
    private boolean composite;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "base_price")
    private BigDecimal basePrice;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Column(name = "profit_margin")
    private BigDecimal profitMargin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductComponentEntity> components = new ArrayList<>();

    public static ProductEntity fromDomain(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.getId();
        entity.code = product.getCode();
        entity.name = product.getName();
        entity.description = product.getDescription();
        entity.categoryId = product.getCategoryId();
        entity.composite = product.isComposite();
        entity.active = product.isActive();
        entity.basePrice = product.getBasePrice();
        entity.purchasePrice = product.getPurchasePrice();
        entity.profitMargin = product.getProfitMargin();
        entity.createdAt = product.getCreatedAt();
        entity.updatedAt = product.getUpdatedAt();
        entity.components = product.getComponents().stream()
            .map(c -> ProductComponentEntity.fromDomain(c, entity))
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return entity;
    }

    public void updateFromDomain(Product product) {
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategoryId();
        this.composite = product.isComposite();
        this.active = product.isActive();
        this.basePrice = product.getBasePrice();
        this.purchasePrice = product.getPurchasePrice();
        this.profitMargin = product.getProfitMargin();
        this.updatedAt = product.getUpdatedAt();
        this.components.clear();
        product.getComponents().forEach(c -> this.components.add(ProductComponentEntity.fromDomain(c, this)));
    }

    public Product toDomain() {
        var domainComponents = this.components.stream()
            .map(ProductComponentEntity::toDomain)
            .toList();
        return Product.reconstitute(id, code, name, description, categoryId,
                composite, active, basePrice, purchasePrice, profitMargin,
                domainComponents, createdAt, updatedAt);
    }
}
