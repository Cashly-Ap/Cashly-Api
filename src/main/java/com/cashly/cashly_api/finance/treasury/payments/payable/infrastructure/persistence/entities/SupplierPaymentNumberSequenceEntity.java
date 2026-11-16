package com.cashly.cashly_api.finance.treasury.payments.payable.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "supplier_payment_number_sequence")
@Getter
@Setter
@NoArgsConstructor
public class SupplierPaymentNumberSequenceEntity {

    @Id
    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;
}
