package com.cashly.cashly_api.customers.balance.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "balance_tx_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BalanceTxSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
