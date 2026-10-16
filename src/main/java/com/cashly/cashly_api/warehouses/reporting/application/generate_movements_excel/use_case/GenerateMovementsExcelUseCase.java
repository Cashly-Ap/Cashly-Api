package com.cashly.cashly_api.warehouses.reporting.application.generate_movements_excel.use_case;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;

public interface GenerateMovementsExcelUseCase {
    byte[] execute(MovementsPdfQuery query);
}
