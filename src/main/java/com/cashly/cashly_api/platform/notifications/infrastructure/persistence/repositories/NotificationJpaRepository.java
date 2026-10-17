package com.cashly.cashly_api.platform.notifications.infrastructure.persistence.repositories;

import com.cashly.cashly_api.platform.notifications.infrastructure.persistence.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndIsReadFalse(UUID userId);
    List<NotificationEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    long countByCustomerIdAndIsReadFalse(UUID customerId);
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") UUID userId);
}
