package com.parsun.parsun_api.reports.inventory.application.port;

import com.parsun.parsun_api.reports.inventory.application.dto.response.CommercialCatalogResponse;
import com.parsun.parsun_api.reports.inventory.application.query.CommercialCatalogQuery;

import java.util.List;

public interface CommercialCatalogRepositoryPort {
    List<CommercialCatalogResponse> findCatalog(CommercialCatalogQuery query);
}
