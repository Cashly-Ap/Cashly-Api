package com.cashly.cashly_api.reports.inventory.application.ports.out;

import com.cashly.cashly_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.reports.inventory.application.dto.query.CommercialCatalogQuery;

import java.util.List;

public interface CommercialCatalogRepositoryPort {
    List<CommercialCatalogResponse> findCatalog(CommercialCatalogQuery query);
}
