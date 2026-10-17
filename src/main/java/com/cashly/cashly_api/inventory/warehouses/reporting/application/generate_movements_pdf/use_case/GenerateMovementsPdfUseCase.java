package com.cashly.cashly_api.inventory.warehouses.reporting.application.generate_movements_pdf.use_case;

import com.cashly.cashly_api.inventory.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;

public interface GenerateMovementsPdfUseCase {
    byte[] execute(MovementsPdfQuery query);
}
