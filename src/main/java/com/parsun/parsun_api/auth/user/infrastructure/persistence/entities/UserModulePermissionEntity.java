package com.parsun.parsun_api.auth.user.infrastructure.persistence.entities;

import com.parsun.parsun_api.auth.user.domain.valueobjects.BusinessModule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "user_module_permissions", indexes = {
    @Index(name = "idx_ump_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserModulePermissionEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", length = 30, nullable = false)
    private BusinessModule module;

    public static UserModulePermissionEntity of(UUID userId, BusinessModule module) {
        UserModulePermissionEntity entity = new UserModulePermissionEntity();
        entity.id = UUID.randomUUID();
        entity.userId = userId;
        entity.module = module;
        return entity;
    }
}
