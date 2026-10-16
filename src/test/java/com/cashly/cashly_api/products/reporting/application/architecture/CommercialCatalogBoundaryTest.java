package com.cashly.cashly_api.products.reporting.application.architecture;
import com.cashly.cashly_api.products.reporting.application.ports.out.CommercialCatalogPdfPort;
import com.cashly.cashly_api.products.reporting.application.generate_commercial_catalog_pdf.GenerateCommercialCatalogPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
class CommercialCatalogBoundaryTest {
    @Test void coreDoesNotInjectSharedStorageAndPdfContractContainsOnlyData() {
        Arrays.stream(GenerateCommercialCatalogPdfUseCaseImpl.class.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().contains("shared.storage"), field.toString()));
        assertEquals(1, CommercialCatalogPdfPort.class.getDeclaredMethods()[0].getParameterCount());
    }
}
