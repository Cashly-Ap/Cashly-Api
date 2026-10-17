package com.cashly.cashly_api.catalog.products.infrastructure.persistence.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product_code_sequence")
public class ProductCodeSequenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }
}
