package com.cashly.cashly_api.purchases.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_number_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseNumberSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
