package com.cashly.cashly_api.platform.auth.mfa.infrastructure;

import com.cashly.cashly_api.platform.auth.mfa.application.MfaStore;
import com.cashly.cashly_api.platform.auth.mfa.domain.MfaAccount;
import java.sql.Timestamp;
import java.util.Arrays;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMfaStore implements MfaStore {
    private final JdbcTemplate jdbc;
    public JdbcMfaStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
    private static java.time.Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
    private final RowMapper<MfaAccount> mapper = (rs, row) -> {
        String hashes = rs.getString("recovery_hashes");
        return new MfaAccount(rs.getString("subject_id"),
                rs.getBoolean("enabled"), rs.getString("secret"),
                instant(rs.getTimestamp("setup_expires_at")),
                rs.getLong("last_step"),
                hashes == null || hashes.isEmpty()
                        ? java.util.List.of()
                        : Arrays.asList(hashes.split(",")),
                rs.getString("challenge_hash"),
                instant(rs.getTimestamp("challenge_expires_at")),
                rs.getInt("failures"),
                instant(rs.getTimestamp("locked_until")));
    };
    public MfaAccount lock(String subjectId) {
        jdbc.update("insert ignore into auth_mfa (subject_id) values (?)",
                subjectId);
        return jdbc.queryForObject(
                "select * from auth_mfa where subject_id = ? for update",
                mapper, subjectId);
    }
    public MfaAccount lockByChallenge(String hash) {
        return jdbc.query(
                "select * from auth_mfa where challenge_hash = ? for update",
                mapper, hash).stream().findFirst().orElse(null);
    }
    public void save(MfaAccount a) {
        jdbc.update(
                "update auth_mfa set enabled=?, secret=?, setup_expires_at=?, last_step=?, recovery_hashes=?, challenge_hash=?, challenge_expires_at=?, failures=?, locked_until=? where subject_id=?",
                a.enabled(), a.secret(), a.setupExpiresAt(), a.lastStep(),
                String.join(",", a.recoveryHashes()), a.challengeHash(),
                a.challengeExpiresAt(), a.failures(), a.lockedUntil(),
                a.subjectId());
    }
}
