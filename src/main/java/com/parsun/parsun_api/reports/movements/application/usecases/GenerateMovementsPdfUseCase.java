package com.parsun.parsun_api.reports.movements.application.usecases;

import com.parsun.parsun_api.reports.movements.application.query.MovementsPdfQuery;

public interface GenerateMovementsPdfUseCase {
    byte[] execute(MovementsPdfQuery query);
}
