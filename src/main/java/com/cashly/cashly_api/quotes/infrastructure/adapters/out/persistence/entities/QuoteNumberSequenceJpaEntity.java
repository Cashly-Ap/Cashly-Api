package com.cashly.cashly_api.quotes.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quote_number_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QuoteNumberSequenceJpaEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
