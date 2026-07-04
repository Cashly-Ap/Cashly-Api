package com.parsun.parsun_api.reports.inventory.application.port;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.shared.storage.domain.FileStorageService;

import java.util.List;

public interface CommercialCatalogPdfPort {
    byte[] generatePdf(List<CommercialCatalogResponse> items, FileStorageService fileStorageService);
}
