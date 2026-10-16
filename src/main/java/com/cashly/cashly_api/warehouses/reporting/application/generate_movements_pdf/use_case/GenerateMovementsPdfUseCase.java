package com.cashly.cashly_api.warehouses.reporting.application.generate_movements_pdf.use_case;

import com.cashly.cashly_api.warehouses.reporting.application.dto.query.model.MovementsPdfQuery;

public interface GenerateMovementsPdfUseCase {
    byte[] execute(MovementsPdfQuery query);
}
