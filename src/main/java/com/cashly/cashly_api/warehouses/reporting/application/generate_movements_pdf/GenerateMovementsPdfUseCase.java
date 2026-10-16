package com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.MovementsPdfQuery;

public interface GenerateMovementsPdfUseCase {
    byte[] execute(MovementsPdfQuery query);
}
