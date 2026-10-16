package com.cashly.cashly_api.purchases.reporting.application.generate_purchase_pdf;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchaseReportQueryGateway;
import com.cashly.cashly_api.purchases.reporting.application.dto.PurchaseReportData;
import com.cashly.cashly_api.purchases.reporting.application.ports.out.PurchasePdfPort;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GeneratePurchasePdfUseCaseImplTest {
    @Test void returnsPdfForTheRequestedPurchaseWithoutChangingProjection() {
        var query = mock(PurchaseReportQueryGateway.class);
        var pdf = mock(PurchasePdfPort.class);
        var id = UUID.randomUUID();
        var data = mock(PurchaseReportData.class);
        var expected = new byte[]{1, 2, 3};
        when(query.find(id)).thenReturn(data);
        when(pdf.generatePdf(data)).thenReturn(expected);
        assertSame(expected, new GeneratePurchasePdfUseCaseImpl(query, pdf).execute(id));
        verify(query).find(id);
        verify(pdf).generatePdf(data);
    }
    @Test void missingPurchasePreservesErrorWithoutGeneratingPdf() {
        var query = mock(PurchaseReportQueryGateway.class);
        var pdf = mock(PurchasePdfPort.class);
        var id = UUID.randomUUID();
        var failure = new IllegalArgumentException("Missing purchase");
        when(query.find(id)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> new GeneratePurchasePdfUseCaseImpl(query, pdf).execute(id)));
        verifyNoInteractions(pdf);
    }
}
