package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.platform.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.platform.auth.user.infrastructure.persistence.entities.UserEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V03UsersSeed implements SeedStep {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1,
            8, 0);
    private static final String ADMIN_PASSWORD_HASH = "$2a$10$WTOBvAu4GKi0O13UQiL3n.62XjcFaQvBiC0awMpv8t.biw7zBR1J.";
    private static final String OPERATOR_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private final EntityManager entityManager;

    public V03UsersSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 3;
    }

    @Override
    public void seed() {
        persistUser(ADMIN_USER, "gabriel@gmail.com", "gabriel", "Gabriel",
                "Administrator", SYSTEM_ADMIN_ROLE, ADMINISTRATION_DEPARTMENT,
                UserRole.SUPER_ADMIN, ADMIN_PASSWORD_HASH);
        persistUser(OPERATOR_USER, "operator@cashly.local", "operator", "Sales",
                "Operator", OPERATOR_ROLE, SALES_DEPARTMENT, UserRole.OPERATOR,
                OPERATOR_PASSWORD_HASH);
    }

    private void persistUser(UUID id, String email, String username,
            String firstName, String lastName, UUID roleId, UUID departmentId,
            UserRole role, String passwordHash) {
        var user = entityManager.find(UserEntity.class, id);
        var newUser = user == null;
        if (newUser) {
            user = new UserEntity();
            user.setId(id);
        }
        user.setEmail(email);
        user.setUserName(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoleId(roleId);
        user.setDepartmentId(departmentId);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(CREATED_AT);
        user.setUpdatedAt(CREATED_AT);
        if (newUser) {
            entityManager.persist(user);
        }
    }
}
