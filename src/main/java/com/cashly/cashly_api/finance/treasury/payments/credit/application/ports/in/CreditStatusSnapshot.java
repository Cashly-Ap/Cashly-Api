package com.cashly.cashly_api.finance.treasury.payments.credit.application.ports.in;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CreditStatusSnapshot(UUID customerId, String customerName,
        String customerRif, BigDecimal creditLimit, BigDecimal pendingBalance,
        BigDecimal customerBalance, BigDecimal availableCredit,
        List<OverdueInvoiceSnapshot> overdueInvoices, boolean hasOverdue,
        boolean canCreateCreditInvoice) {
}
