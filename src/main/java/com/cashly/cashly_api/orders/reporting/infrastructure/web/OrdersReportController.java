package com.cashly.cashly_api.orders.reporting.infrastructure.web;

import com.cashly.cashly_api.orders.reporting.application.dto.model.OrderReportFilter;

import com.cashly.cashly_api.orders.reporting.application.generate_order_pdf.use_case.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf.use_case.GenerateOrdersReportPdfUseCase;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/orders")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'OPERATOR') and hasAuthority('MODULE_SALES')")
public class OrdersReportController {

    private final GenerateOrderPdfUseCase generateOrderPdfUseCase;
    private final GenerateOrdersReportPdfUseCase generateOrdersReportPdfUseCase;


    public OrdersReportController(GenerateOrderPdfUseCase generateOrderPdfUseCase,
                                   GenerateOrdersReportPdfUseCase generateOrdersReportPdfUseCase) {
        this.generateOrderPdfUseCase = generateOrderPdfUseCase;
        this.generateOrdersReportPdfUseCase = generateOrdersReportPdfUseCase;

    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getOrderPdf(@PathVariable UUID id) {
        byte[] pdf = generateOrderPdfUseCase.execute(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pedido-" + id + ".pdf\"")
                .body(pdf);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> getOrdersReportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "SIMPLE") String mode) {

        OrderReportFilter filter = new OrderReportFilter(dateFrom, dateTo, customerId, status, productId, categoryId);
        byte[] pdf = generateOrdersReportPdfUseCase.execute(filter, mode);

        String filename = "reporte-pedidos-" + LocalDate.now().toString() + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}