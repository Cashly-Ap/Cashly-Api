package com.parsun.parsun_api.reports.orders.application.usecases;

import com.parsun.parsun_api.orders.application.dto.response.OrderListResponse;
import com.parsun.parsun_api.reports.orders.application.port.OrdersReportPdfPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateOrdersReportPdfUseCaseImpl Unit Tests")
class GenerateOrdersReportPdfUseCaseImplTest {

    @Mock
    private OrdersReportPdfPort ordersReportPdfPort;

    @InjectMocks
    private GenerateOrdersReportPdfUseCaseImpl useCase;

    @Test
    @DisplayName("should generate orders report PDF with mode")
    void should_generate_orders_report_pdf() {
        List<OrderListResponse> orders = List.of(mock(OrderListResponse.class));
        byte[] expectedPdf = "Orders report PDF".getBytes();

        when(ordersReportPdfPort.generatePdf(orders, "DETAILED")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(orders, "DETAILED");

        assertArrayEquals(expectedPdf, result);
        verify(ordersReportPdfPort).generatePdf(orders, "DETAILED");
    }

    @Test
    @DisplayName("should generate orders report PDF with null mode defaulting to simple")
    void should_generate_orders_report_pdf_with_null_mode() {
        List<OrderListResponse> orders = List.of();
        byte[] expectedPdf = "Default PDF".getBytes();

        when(ordersReportPdfPort.generatePdf(orders, "simple")).thenReturn(expectedPdf);

        byte[] result = useCase.execute(orders, null);

        assertArrayEquals(expectedPdf, result);
        verify(ordersReportPdfPort).generatePdf(orders, "simple");
    }
}
