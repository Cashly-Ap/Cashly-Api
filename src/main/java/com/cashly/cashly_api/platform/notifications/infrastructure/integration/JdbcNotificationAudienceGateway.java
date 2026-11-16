package com.cashly.cashly_api.platform.notifications.infrastructure.integration;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationAudienceGateway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@Component
public class JdbcNotificationAudienceGateway
        implements
            NotificationAudienceGateway {

    private final JdbcTemplate jdbc;

    public JdbcNotificationAudienceGateway(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<UUID> findRecipients(String module, String action,
            UUID excludeUserId) {
        if (module == null || action == null) {
            return List.of();
        }
        var permissionModule = PermissionModule.valueOf(module);
        var permissionAction = PermissionAction.valueOf(action);
        return jdbc.query("""
                select distinct u.id
                  from users u
                  join auth_roles r on r.id = u.role_id
                  left join auth_role_permissions p
                    on p.role_id = r.id
                   and p.module = ?
                   and p.action = ?
                  left join notification_preferences np on np.user_id = u.id
                  left join notification_module_preferences nmp
                    on nmp.user_id = u.id
                   and nmp.module = ?
                 where u.active = true
                   and r.active = true
                   and (r.system_key = 'SYSTEM_ADMIN' or p.role_id is not null)
                   and coalesce(np.enabled, true) = true
                   and coalesce(nmp.enabled, true) = true
                   and (? is null or u.id <> ?)
                """, (rs, row) -> uuid(rs.getBytes(1)), permissionModule.name(),
                permissionAction.name(), permissionModule.name(),
                excludeUserId == null ? null : bytes(excludeUserId),
                excludeUserId == null ? null : bytes(excludeUserId));
    }

    private static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static UUID uuid(byte[] bytes) {
        var buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
