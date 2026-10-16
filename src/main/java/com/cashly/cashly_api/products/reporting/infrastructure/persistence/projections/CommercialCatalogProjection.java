package com.cashly.cashly_api.products.reporting.infrastructure.persistence.projections;

import java.math.BigDecimal;

public interface CommercialCatalogProjection {
    String getProductId();
    String getProductName();
    String getSku();
    String getDescription();
    String getPhoto();
    String getCategoryId();
    String getCategoryName();
    BigDecimal getPrice();
    String getPriceTypeName();
}
