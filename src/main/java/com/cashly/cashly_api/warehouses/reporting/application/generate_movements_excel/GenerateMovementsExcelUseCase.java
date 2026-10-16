package com.cashly.cashly_api.warehouses.reporting.application.generate_movements_excel;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.MovementsPdfQuery;

public interface GenerateMovementsExcelUseCase {
    byte[] execute(MovementsPdfQuery query);
}
