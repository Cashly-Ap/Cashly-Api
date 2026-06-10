package com.parsun.parsun_api.cashregister.domain.entities;

import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterId;
import com.parsun.parsun_api.cashregister.domain.valueobjects.RegisterStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CashRegister {

    private final RegisterId id;
    private final LocalDate closureDate;
    private RegisterStatus status;
    private final UUID openedBy;
    private final String openedByName;
    private final LocalDateTime openedAt;
    private UUID closedBy;
    private String closedByName;
    private LocalDateTime closedAt;
    private UUID verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private BigDecimal totalPayments;
    private BigDecimal totalAdvances;
    private BigDecimal totalReturns;
    private BigDecimal totalCreditNotes;
    private BigDecimal totalDebitNotes;
    private BigDecimal totalInvoices;
    private int paymentCount;
    private int advanceCount;
    private int returnCount;
    private int invoiceCount;
    private BigDecimal expectedClosingBalance;
    private BigDecimal actualClosingBalance;
    private BigDecimal difference;
    private String closingNotes;
    private int version;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<CashRegisterDetail> details;

    private CashRegister(RegisterId id, LocalDate closureDate, RegisterStatus status,
                        UUID openedBy, String openedByName, LocalDateTime openedAt,
                        UUID closedBy, String closedByName, LocalDateTime closedAt,
                        UUID verifiedBy, String verifiedByName, LocalDateTime verifiedAt,
                        BigDecimal totalPayments, BigDecimal totalAdvances,
                        BigDecimal totalReturns, BigDecimal totalCreditNotes, BigDecimal totalDebitNotes,
                        BigDecimal totalInvoices,
                        int paymentCount, int advanceCount, int returnCount, int invoiceCount,
                        BigDecimal expectedClosingBalance, BigDecimal actualClosingBalance,
                        BigDecimal difference, String closingNotes,
                        int version, LocalDateTime createdAt, LocalDateTime updatedAt,
                        List<CashRegisterDetail> details) {
        this.id = id;
        this.closureDate = closureDate;
        this.status = status;
        this.openedBy = openedBy;
        this.openedByName = openedByName;
        this.openedAt = openedAt;
        this.closedBy = closedBy;
        this.closedByName = closedByName;
        this.closedAt = closedAt;
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.verifiedAt = verifiedAt;
        this.totalPayments = totalPayments;
        this.totalAdvances = totalAdvances;
        this.totalReturns = totalReturns;
        this.totalCreditNotes = totalCreditNotes;
        this.totalDebitNotes = totalDebitNotes;
        this.totalInvoices = totalInvoices;
        this.paymentCount = paymentCount;
        this.advanceCount = advanceCount;
        this.returnCount = returnCount;
        this.invoiceCount = invoiceCount;
        this.expectedClosingBalance = expectedClosingBalance;
        this.actualClosingBalance = actualClosingBalance;
        this.difference = difference;
        this.closingNotes = closingNotes;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.details = details;
    }

    // --- Factory: OPEN a new closure ---

    public static CashRegister open(RegisterId id, LocalDate closureDate,
                                   UUID openedBy, String openedByName) {
        Objects.requireNonNull(id, "RegisterId must not be null");
        Objects.requireNonNull(closureDate, "Closure date must not be null");
        Objects.requireNonNull(openedBy, "Opened by must not be null");
        Objects.requireNonNull(openedByName, "Opened by name must not be null");
        if (openedByName.isBlank()) {
            throw new IllegalArgumentException("Opened by name must not be blank");
        }

        LocalDateTime now = LocalDateTime.now();
        return new CashRegister(
                id, closureDate, RegisterStatus.OPEN,
                openedBy, openedByName, now,
                null, null, null,
                null, null, null,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO,
                0, 0, 0, 0,
                BigDecimal.ZERO, null, null, null,
                0, now, now,
                new ArrayList<>()
        );
    }

    // --- Factory: RECONSTITUTE from persistence ---

    public static CashRegister reconstitute(
            RegisterId id, LocalDate closureDate, RegisterStatus status,
            UUID openedBy, String openedByName, LocalDateTime openedAt,
            UUID closedBy, String closedByName, LocalDateTime closedAt,
            UUID verifiedBy, String verifiedByName, LocalDateTime verifiedAt,
            BigDecimal totalPayments, BigDecimal totalAdvances,
            BigDecimal totalReturns, BigDecimal totalCreditNotes, BigDecimal totalDebitNotes,
            BigDecimal totalInvoices,
            int paymentCount, int advanceCount, int returnCount, int invoiceCount,
            BigDecimal expectedClosingBalance, BigDecimal actualClosingBalance,
            BigDecimal difference, String closingNotes,
            int version, LocalDateTime createdAt, LocalDateTime updatedAt,
            List<CashRegisterDetail> details) {
        return new CashRegister(
                id, closureDate, status,
                openedBy, openedByName, openedAt,
                closedBy, closedByName, closedAt,
                verifiedBy, verifiedByName, verifiedAt,
                totalPayments, totalAdvances,
                totalReturns, totalCreditNotes, totalDebitNotes,
                totalInvoices,
                paymentCount, advanceCount, returnCount, invoiceCount,
                expectedClosingBalance, actualClosingBalance,
                difference, closingNotes,
                version, createdAt, updatedAt,
                details != null ? new ArrayList<>(details) : new ArrayList<>()
        );
    }

    // --- Behaviour: apply computed totals (not a state machine step) ---

    public void applyTotals(
            BigDecimal totalPayments, int paymentCount,
            BigDecimal totalAdvances, int advanceCount,
            BigDecimal totalReturns, int returnCount,
            BigDecimal totalCreditNotes,
            BigDecimal totalDebitNotes,
            BigDecimal totalInvoices, int invoiceCount,
            List<CashRegisterDetail> details) {
        if (totalPayments.compareTo(BigDecimal.ZERO) < 0 ||
            totalAdvances.compareTo(BigDecimal.ZERO) < 0 ||
            totalReturns.compareTo(BigDecimal.ZERO) < 0 ||
            totalCreditNotes.compareTo(BigDecimal.ZERO) < 0 ||
            totalDebitNotes.compareTo(BigDecimal.ZERO) < 0 ||
            totalInvoices.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amounts must not be negative");
        }

        this.totalPayments = totalPayments;
        this.paymentCount = paymentCount;
        this.totalAdvances = totalAdvances;
        this.advanceCount = advanceCount;
        this.totalReturns = totalReturns;
        this.returnCount = returnCount;
        this.totalCreditNotes = totalCreditNotes;
        this.totalDebitNotes = totalDebitNotes;
        this.totalInvoices = totalInvoices;
        this.invoiceCount = invoiceCount;

        // expected = payments + advances - returns - credit_notes + debit_notes
        this.expectedClosingBalance = totalPayments
                .add(totalAdvances)
                .subtract(totalReturns)
                .subtract(totalCreditNotes)
                .add(totalDebitNotes);

        this.details.clear();
        if (details != null) {
            this.details.addAll(details);
        }

        this.updatedAt = LocalDateTime.now();
    }

    // --- Behaviour: close ---

    public void close(UUID closedBy, String closedByName, String closingNotes) {
        if (status != RegisterStatus.OPEN) {
            throw new IllegalStateException("Only OPEN closures can be closed");
        }
        Objects.requireNonNull(closedBy, "Closed by must not be null");
        Objects.requireNonNull(closedByName, "Closed by name must not be null");
        if (closedByName.isBlank()) {
            throw new IllegalArgumentException("Closed by name must not be blank");
        }

        this.status = RegisterStatus.CLOSED;
        this.closedBy = closedBy;
        this.closedByName = closedByName;
        this.closedAt = LocalDateTime.now();
        this.closingNotes = closingNotes;
        this.updatedAt = LocalDateTime.now();
    }

    // --- Behaviour: verify ---

    public void verify(UUID verifiedBy, String verifiedByName) {
        if (status != RegisterStatus.CLOSED) {
            throw new IllegalStateException("Only CLOSED closures can be verified");
        }
        Objects.requireNonNull(verifiedBy, "Verified by must not be null");
        Objects.requireNonNull(verifiedByName, "Verified by name must not be null");

        this.status = RegisterStatus.VERIFIED;
        this.verifiedBy = verifiedBy;
        this.verifiedByName = verifiedByName;
        this.verifiedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters ---

    public RegisterId getId() { return id; }
    public LocalDate getClosureDate() { return closureDate; }
    public RegisterStatus getStatus() { return status; }
    public UUID getOpenedBy() { return openedBy; }
    public String getOpenedByName() { return openedByName; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public UUID getClosedBy() { return closedBy; }
    public String getClosedByName() { return closedByName; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public UUID getVerifiedBy() { return verifiedBy; }
    public String getVerifiedByName() { return verifiedByName; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public BigDecimal getTotalPayments() { return totalPayments; }
    public BigDecimal getTotalAdvances() { return totalAdvances; }
    public BigDecimal getTotalReturns() { return totalReturns; }
    public BigDecimal getTotalCreditNotes() { return totalCreditNotes; }
    public BigDecimal getTotalDebitNotes() { return totalDebitNotes; }
    public BigDecimal getTotalInvoices() { return totalInvoices; }
    public int getPaymentCount() { return paymentCount; }
    public int getAdvanceCount() { return advanceCount; }
    public int getReturnCount() { return returnCount; }
    public int getInvoiceCount() { return invoiceCount; }
    public BigDecimal getExpectedClosingBalance() { return expectedClosingBalance; }
    public BigDecimal getActualClosingBalance() { return actualClosingBalance; }
    public BigDecimal getDifference() { return difference; }
    public String getClosingNotes() { return closingNotes; }
    public int getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<CashRegisterDetail> getDetails() {
        return Collections.unmodifiableList(details);
    }
}
