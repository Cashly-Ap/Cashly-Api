package com.cashly.cashly_api.orders.infrastructure.persistence.repositories;

import com.cashly.cashly_api.orders.infrastructure.persistence.entities.OrderNumberSequenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderNumberSequenceJpaRepository extends JpaRepository<OrderNumberSequenceEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OrderNumberSequenceEntity s WHERE s.year = :year")
    Optional<OrderNumberSequenceEntity> findByYearForUpdate(@Param("year") int year);
}
