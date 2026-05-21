package com.parsun.parsun_api.finances.infrastructure.persistence.repositories;

import com.parsun.parsun_api.finances.infrastructure.persistence.entities.ReturnLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReturnLineJpaRepository extends JpaRepository<ReturnLineEntity, UUID> {

    List<ReturnLineEntity> findByReturnId(UUID returnId);
}
