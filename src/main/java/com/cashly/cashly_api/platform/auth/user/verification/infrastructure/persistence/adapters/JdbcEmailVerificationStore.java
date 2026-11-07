package com.cashly.cashly_api.platform.auth.user.verification.infrastructure.persistence.adapters;

import com.cashly.cashly_api.platform.auth.user.verification.application.ports.out.EmailVerificationStore;
import com.cashly.cashly_api.platform.auth.user.verification.domain.entity.VerificationToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcEmailVerificationStore implements EmailVerificationStore {

    private final JdbcTemplate jdbc;

    public JdbcEmailVerificationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static byte[] binary(UUID id) {
        return ByteBuffer.allocate(16).putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static UUID uuid(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private static Timestamp timestamp(Instant value) {
        return value == null ? null : Timestamp.from(value);
    }

    private final RowMapper<VerificationToken> mapper = (rs, row) -> new VerificationToken(
            uuid(rs.getBytes("id")), uuid(rs.getBytes("user_id")),
            rs.getString("token_hash"), instant(rs.getTimestamp("expires_at")),
            instant(rs.getTimestamp("used_at")));

    @Override
    public void invalidateActive(UUID userId, Instant usedAt) {
        jdbc.update(
                "update email_verification_tokens set used_at = ? where user_id = ? and used_at is null",
                timestamp(usedAt), binary(userId));
    }

    @Override
    public void create(VerificationToken token) {
        jdbc.update(
                "insert into email_verification_tokens (id, user_id, token_hash, expires_at, created_at) values (?, ?, ?, ?, ?)",
                binary(token.id()), binary(token.userId()), token.tokenHash(),
                timestamp(token.expiresAt()),
                Timestamp.from(Instant.now()));
    }

    @Override
    public Optional<VerificationToken> findByHash(String tokenHash) {
        return jdbc
                .query("select * from email_verification_tokens where token_hash = ?",
                        mapper, tokenHash)
                .stream().findFirst();
    }

    @Override
    public boolean markUsed(UUID tokenId, Instant usedAt) {
        return jdbc.update(
                "update email_verification_tokens set used_at = ? where id = ? and used_at is null",
                timestamp(usedAt), binary(tokenId)) > 0;
    }

    @Override
    public Optional<Instant> lastIssuedAt(UUID userId) {
        return jdbc
                .query("select created_at from email_verification_tokens where user_id = ? order by created_at desc limit 1",
                        (rs, row) -> instant(rs.getTimestamp("created_at")),
                        binary(userId))
                .stream().findFirst();
    }
}
