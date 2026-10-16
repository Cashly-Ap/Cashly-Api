package com.cashly.cashly_api.orders.reporting.application.generate_orders_report_pdf;

import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportRow;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrdersReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import com.cashly.cashly_api.orders.reporting.application.ports.out.OrderReportQueryGateway;
import com.cashly.cashly_api.orders.reporting.application.dto.OrderReportFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateOrdersReportPdfUseCaseImpl Unit Tests")
class GenerateOrdersReportPdfUseCaseImplTest {

    @Mock
    private OrdersReportPdfPort ordersReportPdfPort;

    @Mock private OrderReportQueryGateway query;
    @InjectMocks
    private GenerateOrdersReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate orders report PDF with mode")
    void should_generate_orders_report_pdf() {
        List<OrderReportRow> orders = List.of(mock(OrderReportRow.class));
        var filter = new OrderReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(orders);
        byte[] expectedPdf = "Orders report PDF".getBytes();

        when(ordersReportPdfPort.generatePdf(orders, "DETAILED")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, "DETAILED");

        assertArrayEquals(expectedPdf, result);
        verify(ordersReportPdfPort).generatePdf(orders, "DETAILED");
    }

    @Test
    @DisplayName("should generate orders report PDF with null mode defaulting to simple")
    void should_generate_orders_report_pdf_with_null_mode() {
        List<OrderReportRow> orders = List.of();
        var filter = new OrderReportFilter(null, null, null, null, null, null);
        when(query.findAll(filter)).thenReturn(orders);
        byte[] expectedPdf = "Default PDF".getBytes();

        when(ordersReportPdfPort.generatePdf(orders, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(filter, null);

        assertArrayEquals(expectedPdf, result);
        verify(ordersReportPdfPort).generatePdf(orders, "simple");
    }
}
