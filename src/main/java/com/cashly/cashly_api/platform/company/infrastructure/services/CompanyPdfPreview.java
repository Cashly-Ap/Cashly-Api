package com.cashly.cashly_api.platform.company.infrastructure.services;

import com.cashly.cashly_api.shared.infrastructure.i18n.ReportMessages;
import com.cashly.cashly_api.platform.company.application.services.CompanyService;
import com.cashly.cashly_api.finance.billing.invoices.reporting.application.dto.model.InvoiceReportData;
import com.cashly.cashly_api.finance.billing.invoices.reporting.infrastructure.pdf.InvoicePdfGenerator;
import com.cashly.cashly_api.shared.infrastructure.pdf.PdfBranding;
import com.cashly.cashly_api.shared.storage.infrastructure.images.SafeImageStorage;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class CompanyPdfPreview {
    private final CompanyService company;

    public CompanyPdfPreview(CompanyService company) {
        this.company = company;
    }

    public byte[] generate(byte[] file, boolean removePdfLogo,
            byte[] companyLogo, boolean removeLogo) {
        var pdfLogo = file == null ? null : SafeImageStorage.normalize(file);
        var navbarLogo = companyLogo == null
                ? null
                : SafeImageStorage.normalize(companyLogo);
        var selected = company.previewDocumentLogo(pdfLogo, removePdfLogo,
                navbarLogo, removeLogo);
        var data = new InvoiceReportData(UUID.randomUUID(),
                ReportMessages.text("platform.company.reports.preview"),
                ReportMessages.text("platform.company.reports.sampleCustomer"),
                "B12345678", "PED-001", "DRAFT",
                ReportMessages.text(
                        "platform.company.reports.sampleDocumentChangesHaveNotBeenSavedYet"),
                null, LocalDateTime.now(), new BigDecimal("150.00"), "CASH",
                new BigDecimal("150.00"),
                List.of(new InvoiceReportData.Line("REF-001", ReportMessages
                        .text("platform.company.reports.sampleProductOrService"),
                        new BigDecimal("2"), new BigDecimal("75.00"),
                        new BigDecimal("150.00"))));
        return new InvoicePdfGenerator(new PdfBranding(() -> selected))
                .generatePdf(data);
    }
}
