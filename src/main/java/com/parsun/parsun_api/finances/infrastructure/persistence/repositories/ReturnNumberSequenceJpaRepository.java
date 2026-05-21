package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnNumberSequenceJpaRepository extends JpaRepository<ReturnNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ReturnNumberSequenceEntity s WHERE s.year = :year")
    Optional<ReturnNumberSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
