package com.cashly.cashly_api.finance.treasury.cash_register.sessions.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cash_registers")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CashRegisterJpaEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "closure_date", nullable = false)
    private LocalDate closureDate;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "opened_by", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID openedBy;

    @Column(name = "opened_by_name", length = 255, nullable = false)
    private String openedByName;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "opening_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "closed_by", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID closedBy;

    @Column(name = "closed_by_name", length = 255)
    private String closedByName;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "total_payments", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPayments;

    @Column(name = "total_advances", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAdvances;

    @Column(name = "total_returns", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalReturns;

    @Column(name = "total_credit_notes", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCreditNotes;

    @Column(name = "total_debit_notes", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalDebitNotes;

    @Column(name = "total_invoices", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInvoices;

    @Column(name = "payment_count", nullable = false)
    private int paymentCount;

    @Column(name = "advance_count", nullable = false)
    private int advanceCount;

    @Column(name = "return_count", nullable = false)
    private int returnCount;

    @Column(name = "invoice_count", nullable = false)
    private int invoiceCount;

    @Column(name = "expected_closing_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal expectedClosingBalance;

    @Column(name = "actual_closing_balance", precision = 15, scale = 2)
    private BigDecimal actualClosingBalance;

    @Column(name = "difference", precision = 15, scale = 2)
    private BigDecimal difference;

    @Column(name = "total_income", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalIncome;

    @Column(name = "total_expense", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalExpense;

    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "closure", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CashRegisterDetailJpaEntity> details = new ArrayList<>();
}
