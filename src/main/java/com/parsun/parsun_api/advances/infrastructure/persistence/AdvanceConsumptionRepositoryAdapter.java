package com.parsun.parsun_api.advances.infrastructure.persistence;

import com.parsun.parsun_api.advances.domain.entities.AdvanceConsumption;
import com.parsun.parsun_api.advances.domain.repositories.AdvanceConsumptionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AdvanceConsumptionRepositoryAdapter implements AdvanceConsumptionRepository {

    private final AdvanceConsumptionJpaRepository jpaRepository;

    public AdvanceConsumptionRepositoryAdapter(AdvanceConsumptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AdvanceConsumption save(AdvanceConsumption consumption) {
        var entity = AdvanceConsumptionJpaMapper.toEntity(consumption);
        var saved = jpaRepository.save(entity);
        return AdvanceConsumptionJpaMapper.toDomain(saved);
    }

    @Override
    public List<AdvanceConsumption> findByAdvanceId(UUID advanceId) {
        return jpaRepository.findByAdvanceId(advanceId)
                .stream()
                .map(AdvanceConsumptionJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AdvanceConsumption> findByInvoiceId(UUID invoiceId) {
        return jpaRepository.findByInvoiceId(invoiceId)
                .stream()
                .map(AdvanceConsumptionJpaMapper::toDomain)
                .toList();
    }
}
