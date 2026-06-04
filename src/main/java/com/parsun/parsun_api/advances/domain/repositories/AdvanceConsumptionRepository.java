package com.parsun.parsun_api.advances.domain.repositories;

import com.parsun.parsun_api.advances.domain.entities.AdvanceConsumption;

import java.util.List;
import java.util.UUID;

public interface AdvanceConsumptionRepository {

    AdvanceConsumption save(AdvanceConsumption consumption);

    List<AdvanceConsumption> findByAdvanceId(UUID advanceId);

    List<AdvanceConsumption> findByInvoiceId(UUID invoiceId);
}
