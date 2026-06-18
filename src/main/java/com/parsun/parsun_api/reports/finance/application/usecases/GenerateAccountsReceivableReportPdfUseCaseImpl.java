package com.parsun.parsun_api.reports.finance.application.usecases;

import com.parsun.parsun_api.receivables.domain.repositories.ReceivablesRepository;
import com.parsun.parsun_api.reports.finance.application.dto.AccountsReceivableReportData;
import com.parsun.parsun_api.reports.finance.application.dto.AccountsReceivableReportData.AccountReceivableRow;
import com.parsun.parsun_api.reports.finance.application.port.AccountsReceivableReportPdfPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GenerateAccountsReceivableReportPdfUseCaseImpl implements GenerateAccountsReceivableReportPdfUseCase {

    private final ReceivablesRepository receivablesRepository;
    private final AccountsReceivableReportPdfPort pdfPort;

    public GenerateAccountsReceivableReportPdfUseCaseImpl(
            ReceivablesRepository receivablesRepository,
            AccountsReceivableReportPdfPort pdfPort) {
        this.receivablesRepository = receivablesRepository;
        this.pdfPort = pdfPort;
    }

    @Override
    public byte[] execute(UUID customerId, boolean overdueOnly) {
        AccountsReceivableReportData data = buildReportData(customerId, overdueOnly);
        return pdfPort.generateReport(data);
    }

    private AccountsReceivableReportData buildReportData(UUID customerId, boolean overdueOnly) {
        List<Object[]> rows = receivablesRepository.findAccountsReceivableForReport(customerId, overdueOnly);

        List<AccountReceivableRow> reportRows = rows.stream()
                .map(this::mapRow)
                .toList();

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal notYetDue = BigDecimal.ZERO;
        BigDecimal overdue = BigDecimal.ZERO;
        int customersExceededCredit = 0;

        for (AccountReceivableRow row : reportRows) {
            BigDecimal rowBalance = row.pendingBalance();
            totalReceivable = totalReceivable.add(rowBalance);

            BigDecimal rowAge0_30 = row.age0_30();
            notYetDue = notYetDue.add(rowAge0_30);

            BigDecimal rowOverdue = row.age31_60()
                    .add(row.age61_90())
                    .add(row.age91_120())
                    .add(row.age120Plus());
            overdue = overdue.add(rowOverdue);

            if (row.pendingBalance().compareTo(row.creditLimit()) > 0) {
                customersExceededCredit++;
            }
        }

        return new AccountsReceivableReportData(
                java.time.LocalDate.now(),
                overdueOnly,
                reportRows,
                new AccountsReceivableReportData.Summary(
                        totalReceivable,
                        notYetDue,
                        overdue,
                        customersExceededCredit
                )
        );
    }

    private AccountReceivableRow mapRow(Object[] columns) {
        return new AccountReceivableRow(
                toUuid(columns[0]),
                (String) columns[1],
                (String) columns[2],
                toBigDecimal(columns[3]),
                toBigDecimal(columns[4]),
                toBigDecimal(columns[5]),
                toBigDecimal(columns[6]),
                toBigDecimal(columns[7]),
                toBigDecimal(columns[8]),
                toBigDecimal(columns[9]),
                toBigDecimal(columns[10])
        );
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        return (BigDecimal) value;
    }

    private static UUID toUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        if (value instanceof byte[] bytes) {
            java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
            return new UUID(bb.getLong(), bb.getLong());
        }
        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to UUID");
    }
}
