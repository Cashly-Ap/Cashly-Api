package com.cashly.cashly_api.platform.audit.infrastructure.integration;

import com.cashly.cashly_api.platform.audit.application.ports.out.AuditActorGateway;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocalAuditActorGateway implements AuditActorGateway {

    private final NamedParameterJdbcTemplate jdbc;

    public LocalAuditActorGateway(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ActorSnapshot> find(UUID actorId) {
        Map<String, Object> parameters = Map.of("id", bytes(actorId));
        List<ActorSnapshot> rows = jdbc.query(
                "select user_name, photo_path, company_id from users where id = :id",
                parameters,
                (row, index) -> new ActorSnapshot(actorId,
                        row.getString("user_name"), row.getString("photo_path"),
                        uuid(row.getBytes("company_id"))));
        return rows.stream().findFirst();
    }

    private static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static UUID uuid(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        var buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
