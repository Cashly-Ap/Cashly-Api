package com.cashly.cashly_api.products.reporting.application.ports.out;

import com.cashly.cashly_api.products.reporting.application.dto.response.CommercialCatalogResponse;
import com.cashly.cashly_api.products.reporting.application.dto.query.CommercialCatalogQuery;

import java.util.List;

public interface CommercialCatalogRepositoryPort {
    List<CommercialCatalogResponse> findCatalog(CommercialCatalogQuery query);
}
