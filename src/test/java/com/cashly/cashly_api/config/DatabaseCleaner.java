package com.cashly.cashly_api.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DatabaseCleaner {

    @PersistenceContext
    private EntityManager entityManager;

    private static final List<String> TABLES = List.of(
        "refresh_tokens",
        "users",
        "differentials",
        "warehouse_stock",
        "warehouses",
        "customer_contact_info",
        "customer_addresses",
        "customers"
    );

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDatabase() {
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        
        for (String table : TABLES) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
            } catch (Exception e) {
                
            }
        }
        
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
