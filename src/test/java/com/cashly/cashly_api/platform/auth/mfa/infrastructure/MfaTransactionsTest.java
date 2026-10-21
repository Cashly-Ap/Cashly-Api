package com.cashly.cashly_api.platform.auth.mfa.infrastructure;

import com.cashly.cashly_api.platform.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.platform.auth.authentication.domain.entity.Authenticable;
import com.cashly.cashly_api.platform.auth.authentication.domain.valueobjects.AuthenticableType;
import com.cashly.cashly_api.platform.auth.mfa.application.MfaCryptography;
import com.cashly.cashly_api.platform.auth.mfa.application.MfaService;
import com.cashly.cashly_api.platform.auth.mfa.application.MfaStore;
import com.cashly.cashly_api.platform.auth.token.application.ports.out.AuthenticableSubjectGateway;
import com.cashly.cashly_api.platform.auth.token.domain.entity.RefreshToken;
import com.cashly.cashly_api.platform.auth.token.domain.repository.RefreshTokenRepository;
import com.cashly.cashly_api.platform.auth.token.domain.services.TokenService;
import com.cashly.cashly_api.platform.auth.token.domain.valueobjects.RefreshTokenId;
import com.cashly.cashly_api.platform.auth.user.domain.services.PasswordEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(MfaTransactionsTest.Config.class)
@EnabledIfEnvironmentVariable(named = "MFA_TEST_JDBC_URL", matches = ".+")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MfaTransactionsTest {
    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource(System.getenv("MFA_TEST_JDBC_URL"),
                System.getenv("SPRING_DATASOURCE_USERNAME"), System.getenv("SPRING_DATASOURCE_PASSWORD"));
        }
        @Bean JdbcTemplate jdbc(DataSource source) { return new JdbcTemplate(source); }
        @Bean PlatformTransactionManager transactions(DataSource source) { return new DataSourceTransactionManager(source); }
        @Bean MfaStore store(JdbcTemplate jdbc) { return new JdbcMfaStore(jdbc); }
        @Bean MfaCryptography crypto() { return new TotpCryptography(Base64.getEncoder().encodeToString(new byte[32])); }
        @Bean AuthenticableSubjectGateway subjects() { return mock(AuthenticableSubjectGateway.class); }
        @Bean PasswordEncoder passwords() { return mock(PasswordEncoder.class); }
        @Bean RefreshTokenRepository refresh() { return mock(RefreshTokenRepository.class); }
        @Bean TokenService tokens() { return mock(TokenService.class); }
        @Bean MfaService service(MfaStore store, MfaCryptography crypto, AuthenticableSubjectGateway subjects,
                PasswordEncoder passwords, RefreshTokenRepository refresh, TokenService tokens) {
            return new MfaService(store, crypto, subjects, passwords, refresh, tokens);
        }
    }

    @Autowired MfaService service;
    @Autowired MfaCryptography crypto;
    @Autowired JdbcTemplate jdbc;
    @Autowired AuthenticableSubjectGateway subjects;
    @Autowired TokenService tokens;
    private String subjectId;
    private String secret;
    private Authenticable subject;

    @BeforeAll
    void createSchema() throws Exception {
        assertEquals("mfa_verification", jdbc.queryForObject("select database()", String.class),
            "MFA integration tests require the isolated mfa_verification schema");
        jdbc.execute("drop table if exists auth_mfa");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/migration/V28__create_auth_mfa.sql")));
    }

    @BeforeEach
    void setup() {
        reset(subjects, tokens);
        subjectId = UUID.randomUUID().toString();
        secret = crypto.newSecret();
        subject = mock(Authenticable.class);
        when(subject.getSubjectId()).thenReturn(subjectId);
        when(subject.canAuthenticate()).thenReturn(true);
        when(subjects.resolve(subjectId, AuthenticableType.USER)).thenReturn(subject);
        when(tokens.generateAccessToken(subject)).thenReturn("access-token");
        when(tokens.generateRefreshToken(subject)).thenAnswer(invocation -> new RefreshToken(
            RefreshTokenId.generate(), subjectId, AuthenticableType.USER, UUID.randomUUID().toString(), LocalDateTime.now().plusDays(7)));
        jdbc.update("insert into auth_mfa (subject_id, enabled, secret, recovery_hashes) values (?, true, ?, ?)",
            subjectId, crypto.encrypt(secret), crypto.hash("recovery-code"));
    }

    @AfterEach
    void cleanup() { jdbc.update("delete from auth_mfa where subject_id = ?", subjectId); }

    @Test
    void five_separate_failed_transactions_persist_and_lock_the_next_login() {
        String challenge = service.challengeIfEnabled(subject).challengeToken();
        for (int index = 1; index <= 5; index++) {
            assertThrows(InvalidCredentialsException.class, () -> service.verify(challenge, "invalid"));
            assertEquals(index, jdbc.queryForObject("select failures from auth_mfa where subject_id = ?", Integer.class, subjectId));
        }
        assertThrows(InvalidCredentialsException.class, () -> service.challengeIfEnabled(subject));
        assertEquals(5, jdbc.queryForObject("select failures from auth_mfa where subject_id = ?", Integer.class, subjectId));
        verifyNoInteractions(tokens);
    }

    @Test
    void concurrent_verifications_accept_one_recovery_code_and_one_challenge_once() throws Exception {
        String challenge = service.challengeIfEnabled(subject).challengeToken();
        assertEquals(1, concurrentSuccesses(() -> service.verify(challenge, "recovery-code")));
        assertEquals("", jdbc.queryForObject("select recovery_hashes from auth_mfa where subject_id = ?", String.class, subjectId));
        assertNull(jdbc.queryForObject("select challenge_hash from auth_mfa where subject_id = ?", String.class, subjectId));
        String nextChallenge = service.challengeIfEnabled(subject).challengeToken();
        assertThrows(InvalidCredentialsException.class, () -> service.verify(nextChallenge, "recovery-code"));
        verify(tokens, times(1)).generateAccessToken(subject);
    }

    @Test
    void concurrent_totp_verifications_issue_one_session_and_reject_step_reuse() throws Exception {
        String challenge = service.challengeIfEnabled(subject).challengeToken();
        String code = ((TotpCryptography) crypto).totp(secret, Instant.now().getEpochSecond() / 30);
        assertEquals(1, concurrentSuccesses(() -> service.verify(challenge, code)));
        String nextChallenge = service.challengeIfEnabled(subject).challengeToken();
        assertThrows(InvalidCredentialsException.class, () -> service.verify(nextChallenge, code));
        verify(tokens, times(1)).generateAccessToken(subject);
    }

    private int concurrentSuccesses(Callable<?> request) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> attempt = () -> {
                assertTrue(start.await(10, TimeUnit.SECONDS));
                try { request.call(); return true; }
                catch (InvalidCredentialsException expected) { return false; }
            };
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            start.countDown();
            return (first.get(10, TimeUnit.SECONDS) ? 1 : 0) + (second.get(10, TimeUnit.SECONDS) ? 1 : 0);
        }
    }
}
