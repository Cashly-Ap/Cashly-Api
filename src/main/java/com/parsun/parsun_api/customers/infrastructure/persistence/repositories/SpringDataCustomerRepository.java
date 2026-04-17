package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, UUID> {

    @Query("SELECT MAX(c.code) FROM CustomerEntity c")
    Optional<Long> findMaxCode();
}
