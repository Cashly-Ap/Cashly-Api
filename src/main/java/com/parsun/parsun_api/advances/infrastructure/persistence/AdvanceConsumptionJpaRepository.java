package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.infrastructure.persistence.entities.AdvanceConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdvanceConsumptionJpaRepository extends JpaRepository<AdvanceConsumptionEntity, UUID> {

    List<AdvanceConsumptionEntity> findByAdvanceId(UUID advanceId);

    List<AdvanceConsumptionEntity> findByInvoiceId(UUID invoiceId);
}
