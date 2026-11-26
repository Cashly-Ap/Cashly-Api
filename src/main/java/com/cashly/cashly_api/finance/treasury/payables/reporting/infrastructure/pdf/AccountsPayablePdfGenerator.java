package com.cashly.cashly_api.finance.treasury.payables.reporting.infrastructure.pdf;

import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtListResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.dto.response.SupplierDebtSummaryResponse;
import com.cashly.cashly_api.finance.treasury.payables.application.ports.out.PayablesQueryGateway;
import com.cashly.cashly_api.finance.treasury.payables.reporting.application.ports.out.AccountsPayableReportPdfPort;
import com.cashly.cashly_api.shared.infrastructure.pdf.FastTablePdfRenderer;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AccountsPayablePdfGenerator
        implements AccountsPayableReportPdfPort {

    private static final DateTimeFormatter DATE = DateTimeFormatter
            .ofPattern("dd/MM/yyyy");

    private final PdfBranding branding;
    private final PayablesQueryGateway gateway;

    public AccountsPayablePdfGenerator(PdfBranding branding,
            PayablesQueryGateway gateway) {
        this.branding = branding;
        this.gateway = gateway;
    }

    @Override
    public byte[] generate(UUID supplierId, boolean overdueOnly) {
        List<SupplierDebtListResponse> rows = loadRows(supplierId,
                overdueOnly);
        List<FastTablePdfRenderer.Column> columns = List.of(
                new FastTablePdfRenderer.Column("Proveedor", 4f,
                        TextAlignment.LEFT),
                new FastTablePdfRenderer.Column("RIF", 2.5f,
                        TextAlignment.CENTER),
                new FastTablePdfRenderer.Column("Límite crédito", 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("Deuda", 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("Crédito disponible", 2.5f,
                        TextAlignment.RIGHT),
                new FastTablePdfRenderer.Column("Vence", 2.5f,
                        TextAlignment.CENTER));
        BigDecimal total = BigDecimal.ZERO;
        List<String[]> reportRows = new ArrayList<>(rows.size());
        for (SupplierDebtListResponse row : rows) {
            total = total.add(row.totalDebt());
            reportRows.add(new String[]{row.supplierName(),
                    row.supplierRif() != null ? row.supplierRif() : "",
                    money(row.creditLimit()), money(row.totalDebt()),
                    money(row.availableCredit()),
                    row.oldestDueDate() != null
                            ? row.oldestDueDate().format(DATE)
                            : "-"});
        }
        return FastTablePdfRenderer.render(branding, PageSize.A4.rotate(),
                "Cuentas por Pagar",
                "Fecha: " + LocalDate.now().format(DATE), columns, reportRows,
                List.of("Total por pagar: " + money(total)),
                "Página ", " de ");
    }

    private List<SupplierDebtListResponse> loadRows(UUID supplierId,
            boolean overdueOnly) {
        if (supplierId != null) {
            SupplierDebtSummaryResponse detail = gateway
                    .findSupplierDebt(supplierId);
            SupplierDebtListResponse row = new SupplierDebtListResponse(
                    detail.supplierId(), detail.supplierName(),
                    detail.supplierRif(), detail.totalDebt(),
                    detail.pendingPaymentsAmount(), detail.pendingPaymentsCount(),
                    detail.purchasesWithDebt().stream()
                            .map(p -> p.dueDate()).filter(d -> d != null)
                            .min(LocalDate::compareTo).orElse(null),
                    detail.creditDays(), detail.creditLimit(),
                    detail.availableCredit(), detail.supplierBalance());
            return matches(row, overdueOnly) ? List.of(row) : List.of();
        }
        List<SupplierDebtListResponse> content = gateway
                .findSuppliersWithDebt(null, 0, 1000, null, null).content();
        List<SupplierDebtListResponse> filtered = new ArrayList<>();
        for (SupplierDebtListResponse row : content) {
            if (matches(row, overdueOnly)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private boolean matches(SupplierDebtListResponse row, boolean overdueOnly) {
        return !overdueOnly || (row.oldestDueDate() != null
                && row.oldestDueDate().isBefore(LocalDate.now()));
    }

    private String money(BigDecimal value) {
        return "$" + (value != null ? value : BigDecimal.ZERO)
                .setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
