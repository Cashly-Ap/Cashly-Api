package com.cashly.cashly_api.notifications.infrastructure.persistence.adapters;

import com.cashly.cashly_api.notifications.infrastructure.persistence.repositories.NotificationJpaRepository;

import com.cashly.cashly_api.notifications.domain.entity.Notification;
import com.cashly.cashly_api.notifications.domain.repository.NotificationRepository;
import com.cashly.cashly_api.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.notifications.infrastructure.persistence.mappers.NotificationJpaMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        var entity = NotificationJpaMapper.toEntity(notification);
        var saved = jpaRepository.save(entity);
        return NotificationJpaMapper.toDomain(saved);
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationJpaMapper::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByUserId(UUID userId) {
        return jpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public List<Notification> findByCustomerIdOrderByCreatedAtDesc(UUID customerId) {
        return jpaRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(NotificationJpaMapper::toDomain)
                .toList();
    }

    @Override
    public long countUnreadByCustomerId(UUID customerId) {
        return jpaRepository.countByCustomerIdAndIsReadFalse(customerId);
    }

    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value())
                .map(NotificationJpaMapper::toDomain);
    }

    @Override
    public void markAllAsReadByUserId(UUID userId) {
        jpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteById(NotificationId id) {
        jpaRepository.deleteById(id.value());
    }
}
