package com.parsun.parsun_api.credit_notes.infrastructure.persistence.repositories;

import com.parsun.parsun_api.credit_notes.infrastructure.persistence.entities.CreditNoteNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CreditNoteNumberSequenceJpaRepository extends JpaRepository<CreditNoteNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM CreditNoteNumberSequenceEntity s WHERE s.year = :year")
    Optional<CreditNoteNumberSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
