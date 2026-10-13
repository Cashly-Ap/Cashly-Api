package com.cashly.cashly_api.debit_notes.infrastructure.adapters.out.persistence.repositories;

import com.cashly.cashly_api.debit_notes.infrastructure.adapters.out.persistence.entities.DebitNoteNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DebitNoteNumberSequenceJpaRepository extends JpaRepository<DebitNoteNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DebitNoteNumberSequenceEntity s WHERE s.year = :year")
    Optional<DebitNoteNumberSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
