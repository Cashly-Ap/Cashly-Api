package com.cashly.cashly_api.returns.infrastructure.adapters.out.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "return_number_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReturnNumberSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
