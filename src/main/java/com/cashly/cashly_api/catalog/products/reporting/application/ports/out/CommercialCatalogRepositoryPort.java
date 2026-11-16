package com.cashly.cashly_api.catalog.products.reporting.application.ports.out;

import com.cashly.cashly_api.catalog.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.catalog.products.reporting.application.dto.query.model.CommercialCatalogQuery;

import java.util.List;

public interface CommercialCatalogRepositoryPort {
    List<CommercialCatalogResponse> findCatalog(CommercialCatalogQuery query);
}
