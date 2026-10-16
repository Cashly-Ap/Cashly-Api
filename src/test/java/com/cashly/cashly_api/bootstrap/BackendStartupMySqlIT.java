package com.cashly.cashly_api.bootstrap;

import com.cashly.cashly_api.CashlyApplication;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Real server, security filters, Flyway and repositories; never connects to a development database. */
@SpringBootTest(classes = CashlyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = BackendStartupMySqlIT.IsolatedDatabaseOnly.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackendStartupMySqlIT {
    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder passwords;
    private String adminToken;
    private String restrictedToken;

    public static class IsolatedDatabaseOnly implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            String url = context.getEnvironment().getProperty("spring.datasource.url");
            if (!"jdbc:mysql://suite-db:3306/screaming_suite".equals(url)) {
                throw new IllegalStateException("BackendStartupMySqlIT requires the isolated screaming verification harness");
            }
        }
    }

    record Endpoint(String module, String path) {}

    static Stream<Endpoint> endpoints() {
        return Stream.of(
            new Endpoint("auth", "/api/users"),
            new Endpoint("cash_register", "/api/cash-register/history"),
            new Endpoint("categories", "/api/categories"),
            new Endpoint("credit_notes", "/api/credit-notes"),
            new Endpoint("customers", "/api/customers"),
            new Endpoint("dashboard", "/api/dashboard/summary"),
            new Endpoint("debit_notes", "/api/debit-notes"),
            new Endpoint("invoices", "/api/invoices"),
            new Endpoint("notifications", "/api/notifications"),
            new Endpoint("orders", "/api/orders"),
            new Endpoint("payment_methods", "/api/payment-methods"),
            new Endpoint("payments", "/api/payments"),
            new Endpoint("price_types", "/api/price-types"),
            new Endpoint("products", "/api/products"),
            new Endpoint("purchases", "/api/purchases"),
            new Endpoint("quotes", "/api/quotes"),
            new Endpoint("receivables", "/api/receivables/summary"),
            new Endpoint("warehouses.reporting", "/api/reports/inventory/catalog"),
            new Endpoint("returns", "/api/returns"),
            new Endpoint("suppliers", "/api/suppliers"),
            new Endpoint("warehouses", "/api/warehouses")
        );
    }

    @BeforeAll
    void createSyntheticUsersAndLoginThroughRealHttp() {
        adminToken = createAndLogin("SUPER_ADMIN");
        restrictedToken = createAndLogin("OPERATOR");
    }

    private String createAndLogin(String role) {
        UUID id = UUID.randomUUID();
        String email = "structure-" + id + "@example.test";
        String password = "Synthetic-Structure-2026!";
        byte[] binaryId = ByteBuffer.allocate(16).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits()).array();
        jdbc.update("INSERT INTO users (id,email,user_name,password_hash,role,active,email_verified,created_at,updated_at) VALUES (?,?,?,?,?,true,true,NOW(),NOW())",
            binaryId, email, "Structure verification", passwords.encode(password), role);
        ResponseEntity<JsonNode> response = http.postForEntity("/api/auth/login", Map.of("email", email, "password", password), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        String token = response.getBody().path("accessToken").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    @ParameterizedTest(name = "{0}: authorized real HTTP contract")
    @MethodSource("endpoints")
    void shouldResolveEveryModuleThroughAuthorizedHttp(Endpoint endpoint) {
        ResponseEntity<String> response = get(endpoint.path(), adminToken);
        assertThat(response.getStatusCode()).as(endpoint.module()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).as(endpoint.module()).isNotNull().matches(type -> type.isCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @ParameterizedTest(name = "{0}: anonymous access rejected")
    @MethodSource("endpoints")
    void shouldRequireAuthenticationForEveryModule(Endpoint endpoint) {
        assertThat(get(endpoint.path(), null).getStatusCode()).as(endpoint.module()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    static Stream<Endpoint> permissionEndpoints() {
        return endpoints().filter(endpoint -> !endpoint.module().equals("notifications"));
    }

    @ParameterizedTest(name = "{0}: missing module permission rejected")
    @MethodSource("permissionEndpoints")
    void shouldRejectOperatorWithoutModulePermissions(Endpoint endpoint) {
        assertThat(get(endpoint.path(), restrictedToken).getStatusCode()).as(endpoint.module()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturnNotFoundForMissingWarehouseThroughRealPersistence() {
        assertThat(get("/api/warehouses/" + UUID.randomUUID(), adminToken).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<String> get(String path, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) headers.setBearerAuth(token);
        return http.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
