package com.cashly.cashly_api.auth.user.infrastructure.persistence.entities;

import com.cashly.cashly_api.auth.user.domain.valueobjects.BusinessModule;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", length = 30, nullable = false)
    private BusinessModule module;

    public static UserModulePermissionEntity of(UserEntity user, BusinessModule module) {
        UserModulePermissionEntity entity = new UserModulePermissionEntity();
        entity.id = UUID.randomUUID();
        entity.user = user;
        entity.module = module;
        return entity;
    }
}
