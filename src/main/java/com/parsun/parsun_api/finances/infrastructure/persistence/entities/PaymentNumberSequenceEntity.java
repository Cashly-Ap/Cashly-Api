package com.parsun.parsun_api.finances.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_number_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PaymentNumberSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
