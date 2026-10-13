package com.cashly.cashly_api.reports.orders.infrastructure.adapters.in.controllers;

import com.cashly.cashly_api.reports.orders.application.dto.OrderReportRow;
import com.cashly.cashly_api.reports.orders.application.dto.OrderReportFilter;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrderPdfUseCase;
import com.cashly.cashly_api.reports.orders.application.usecases.GenerateOrdersReportPdfUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdersReportController Unit Tests")
class OrdersReportControllerTest {

    @Mock
    private GenerateOrderPdfUseCase generateOrderPdfUseCase;

    @Mock
    private GenerateOrdersReportPdfUseCase generateOrdersReportPdfUseCase;



    @InjectMocks
    private OrdersReportController controller;

    @Test
    @DisplayName("should return order PDF as attachment")
    void should_get_order_pdf() {
        UUID orderId = UUID.randomUUID();
        byte[] pdfContent = "Order PDF".getBytes();

        when(generateOrderPdfUseCase.execute(orderId)).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getOrderPdf(orderId);

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("pedido-" + orderId + ".pdf"));
        assertArrayEquals(pdfContent, result.getBody());
        assertEquals(200, result.getStatusCodeValue());
        verify(generateOrderPdfUseCase).execute(orderId);
    }

    @Test
    @DisplayName("should return orders report PDF as attachment")
    void should_get_orders_report_pdf() {
        UUID customerId = UUID.randomUUID();
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        byte[] pdfContent = "Orders report PDF".getBytes();
        List<OrderReportRow> orders = List.of();

        var filter = new OrderReportFilter(dateFrom, dateTo, customerId, "PENDING", productId, categoryId);
        when(generateOrdersReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getOrdersReportPdf(
                dateFrom, dateTo, customerId, "PENDING", productId, categoryId, "SIMPLE");

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
        assertTrue(result.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)
                .contains("reporte-pedidos-"));
        assertArrayEquals(pdfContent, result.getBody());

        verify(generateOrdersReportPdfUseCase).execute(filter, "SIMPLE");
    }

    @Test
    @DisplayName("should return orders report PDF without optional params")
    void should_get_orders_report_pdf_without_optional_params() {
        byte[] pdfContent = "PDF no filters".getBytes();
        List<OrderReportRow> orders = List.of();

        var filter = new OrderReportFilter(null, null, null, null, null, null);
        when(generateOrdersReportPdfUseCase.execute(filter, "SIMPLE")).thenReturn(pdfContent);

        ResponseEntity<byte[]> result = controller.getOrdersReportPdf(
                null, null, null, null, null, null, "SIMPLE");

        assertArrayEquals(pdfContent, result.getBody());
        verify(generateOrdersReportPdfUseCase).execute(filter, "SIMPLE");
    }
}
