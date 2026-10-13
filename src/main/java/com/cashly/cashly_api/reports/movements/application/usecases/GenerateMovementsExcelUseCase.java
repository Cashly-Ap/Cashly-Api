package com.cashly.cashly_api.reports.movements.application.usecases;

import com.cashly.cashly_api.reports.movements.application.dto.query.MovementsPdfQuery;

public interface GenerateMovementsExcelUseCase {
    byte[] execute(MovementsPdfQuery query);
}
