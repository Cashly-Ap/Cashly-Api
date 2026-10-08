package com.cashly.cashly_api.reports.inventory.application;
import com.cashly.cashly_api.reports.inventory.application.port.CommercialCatalogPdfPort;
import com.cashly.cashly_api.reports.inventory.application.usecases.GenerateCommercialCatalogPdfUseCaseImpl;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
class CommercialCatalogBoundaryTest {
    @Test void coreDoesNotInjectSharedStorageAndPdfContractContainsOnlyData() {
        Arrays.stream(GenerateCommercialCatalogPdfUseCaseImpl.class.getDeclaredFields()).forEach(field -> assertFalse(field.getType().getName().contains("shared.storage"), field.toString()));
        assertEquals(1, CommercialCatalogPdfPort.class.getDeclaredMethods()[0].getParameterCount());
    }
}
