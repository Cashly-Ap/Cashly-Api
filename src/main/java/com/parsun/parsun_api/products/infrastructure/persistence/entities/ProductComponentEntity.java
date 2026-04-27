package com.parsun.parsun_api.products.infrastructure.persistence.entities;

import com.parsun.parsun_api.products.domain.entities.ProductComponent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "product_components")
@Getter
@Setter
@NoArgsConstructor
public class ProductComponentEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @Column(name = "parent_product_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID parentProductId;

    @Column(name = "component_product_id", nullable = false, columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID componentProductId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public static ProductComponentEntity fromDomain(ProductComponent component) {
        ProductComponentEntity entity = new ProductComponentEntity();
        entity.id = component.getId();
        entity.parentProductId = component.getParentProductId();
        entity.componentProductId = component.getComponentProductId();
        entity.quantity = component.getQuantity();
        return entity;
    }

    public ProductComponent toDomain() {
        return new ProductComponent(id, parentProductId, componentProductId, quantity);
    }
}
