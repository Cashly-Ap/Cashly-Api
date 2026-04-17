package com.parsun.parsun_api.auth.user.infrastructure.persistence.entities;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Email;
import com.parsun.parsun_api.auth.user.domain.valueobjects.Password;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserName;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_active", columnList = "active"),
    @Index(name = "idx_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "passwordHash")
public class UserEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "user_name", length = 100, nullable = false)
    private String userName;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role = UserRole.OPERATOR;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserEntity fromDomain(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserEntity entity = new UserEntity();
        entity.id = user.getId().getValue();
        entity.email = user.getEmail().getValue();
        entity.userName = user.getUserName().getValue();
        entity.passwordHash = user.getPassword().getHashedValue();
        entity.role = user.getRole();
        entity.active = user.isActive();
        entity.emailVerified = user.isEmailVerified();
        entity.createdAt = user.getCreatedAt();
        entity.updatedAt = user.getUpdatedAt();

        return entity;
    }

    public void updateFromDomain(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        this.email = user.getEmail().getValue();
        this.userName = user.getUserName().getValue();
        this.passwordHash = user.getPassword().getHashedValue();
        this.role = user.getRole();
        this.active = user.isActive();
        this.emailVerified = user.isEmailVerified();
        this.updatedAt = user.getUpdatedAt();
    }

    public User toDomain() {
        UserId userId = new UserId(this.id);
        Email domainEmail = new Email(this.email);
        UserName domainUserName = new UserName(this.userName);
        Password domainPassword = Password.fromHash(this.passwordHash);

        User user = new User(userId, domainEmail, domainUserName, domainPassword, this.role);

        try {
            java.lang.reflect.Field activeField = User.class.getDeclaredField("active");
            java.lang.reflect.Field emailVerifiedField = User.class.getDeclaredField("emailVerified");
            java.lang.reflect.Field createdAtField = User.class.getDeclaredField("createdAt");
            java.lang.reflect.Field updatedAtField = User.class.getDeclaredField("updatedAt");

            activeField.setAccessible(true);
            emailVerifiedField.setAccessible(true);
            createdAtField.setAccessible(true);
            updatedAtField.setAccessible(true);

            activeField.set(user, this.active);
            emailVerifiedField.set(user, this.emailVerified);
            createdAtField.set(user, this.createdAt);
            updatedAtField.set(user, this.updatedAt);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set fields on domain entity", e);
        }

        return user;
    }
}

