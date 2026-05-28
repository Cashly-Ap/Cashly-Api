package com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "credit_note_number_sequence")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CreditNoteNumberSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    @EqualsAndHashCode.Include
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
