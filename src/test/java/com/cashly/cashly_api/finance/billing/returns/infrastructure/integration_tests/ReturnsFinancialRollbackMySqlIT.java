package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration_tests;

import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnBalanceGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnCashGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInventoryGateway;
import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInvoiceAdjustmentGateway;
import com.cashly.cashly_api.finance.billing.returns.application.create.request.CreateReturnRequest;
import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReturnsFinancialRollbackMySqlIT {

    private static final String DATABASE_URL_PROPERTY = "returns.mysql.url";
    private static final String DATABASE_USERNAME_PROPERTY = "returns.mysql.username";
    private static final String DATABASE_PASSWORD_PROPERTY = "returns.mysql.password";

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> requiredProperty(DATABASE_URL_PROPERTY,
                        "RETURNS_MYSQL_URL"));
        registry.add("spring.datasource.username",
                () -> requiredProperty(DATABASE_USERNAME_PROPERTY,
                        "RETURNS_MYSQL_USERNAME"));
        registry.add("spring.datasource.password",
                () -> requiredProperty(DATABASE_PASSWORD_PROPERTY,
                        "RETURNS_MYSQL_PASSWORD"));
        registry.add("spring.datasource.driver-class-name",
                () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.docker.compose.enabled", () -> "false");
        registry.add("storage.type", () -> "local");
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private CreateReturnUseCase createReturn;

    private Fixture fixture;

    @MockitoSpyBean
    private ReturnCashGateway cash;

    @MockitoSpyBean
    private ReturnInventoryGateway inventory;

    @MockitoSpyBean
    private ReturnInvoiceAdjustmentGateway invoiceAdjustments;

    @MockitoSpyBean
    private ReturnBalanceGateway balances;

    @ParameterizedTest(name = "rollback financiera MySQL al fallar después de {0}")
    @EnumSource(FailureStage.class)
    void should_rollback_return_balance_cash_stock_and_invoice_changes_when_a_stage_fails(
            FailureStage failureStage) {
        fixture = givenAnInvoiceWithAppliedPaymentAndAvailableStock();
        failAfterRealOperation(failureStage);

        assertThrows(InjectedFailure.class,
                () -> createReturn.execute(request(fixture), fixture.actorId(),
                        "Rollback Operator"));

        assertDatabaseStateIsUnchanged(fixture);
    }

    @AfterEach
    void cleanFixture() {
        if (fixture == null) {
            return;
        }
        jdbc.update("DELETE FROM returns WHERE invoice_id = UUID_TO_BIN(?)",
                fixture.invoiceId().toString());
        jdbc.update("DELETE FROM return_number_sequence");
        jdbc.update(
                "DELETE FROM cash_register_movements WHERE register_id = UUID_TO_BIN(?)",
                fixture.registerId().toString());
        jdbc.update("DELETE FROM cash_registers WHERE id = UUID_TO_BIN(?)",
                fixture.registerId().toString());
        jdbc.update(
                "DELETE FROM stock_movements WHERE warehouse_id = UUID_TO_BIN(?) AND product_id = UUID_TO_BIN(?)",
                fixture.warehouseId().toString(),
                fixture.productId().toString());
        jdbc.update(
                "DELETE FROM warehouse_stock WHERE warehouse_id = UUID_TO_BIN(?) AND product_id = UUID_TO_BIN(?)",
                fixture.warehouseId().toString(),
                fixture.productId().toString());
        jdbc.update(
                "DELETE FROM balance_transactions WHERE customer_id = UUID_TO_BIN(?)",
                fixture.customerId().toString());
        jdbc.update(
                "DELETE FROM customer_balances WHERE customer_id = UUID_TO_BIN(?)",
                fixture.customerId().toString());
        jdbc.update(
                "DELETE FROM payment_allocations WHERE invoice_id = UUID_TO_BIN(?)",
                fixture.invoiceId().toString());
        jdbc.update("DELETE FROM payments WHERE id = UUID_TO_BIN(?)",
                fixture.paymentId().toString());
        jdbc.update(
                "DELETE FROM invoice_lines WHERE invoice_id = UUID_TO_BIN(?)",
                fixture.invoiceId().toString());
        jdbc.update("DELETE FROM invoices WHERE id = UUID_TO_BIN(?)",
                fixture.invoiceId().toString());
        jdbc.update("DELETE FROM warehouses WHERE id = UUID_TO_BIN(?)",
                fixture.warehouseId().toString());
        jdbc.update("DELETE FROM products WHERE id = UUID_TO_BIN(?)",
                fixture.productId().toString());
        jdbc.update("DELETE FROM categories WHERE id = UUID_TO_BIN(?)",
                fixture.categoryId().toString());
        jdbc.update("DELETE FROM customers WHERE id = UUID_TO_BIN(?)",
                fixture.customerId().toString());
        jdbc.update("DELETE FROM users WHERE id = UUID_TO_BIN(?)",
                fixture.actorId().toString());
        fixture = null;
    }

    private Fixture givenAnInvoiceWithAppliedPaymentAndAvailableStock() {
        String version = jdbc.queryForObject("SELECT @@version_comment",
                String.class);
        if (version == null || !version.toLowerCase().contains("mysql")) {
            throw new IllegalStateException(
                    "This integration test requires a real MySQL database");
        }

        Fixture fixture = Fixture.create();
        this.fixture = fixture;
        LocalDateTime now = LocalDateTime.now();

        jdbc.update(
                """
                        INSERT INTO users (id, email, user_name, password_hash, role, first_name, last_name, phone, role_id, department_id, active, email_verified, created_at, updated_at)
                        VALUES (UUID_TO_BIN(?), ?, ?, ?, 'OPERATOR', 'Rollback', 'Operator', NULL, UNHEX('90000000000000000000000000000003'), UNHEX('91000000000000000000000000000001'), TRUE, TRUE, ?, ?)
                        """,
                fixture.actorId().toString(),
                fixture.actorId() + "@returns-it.test", "Rollback Operator",
                "unused-password", now, now);
        jdbc.update(
                "INSERT INTO categories (id, name, description) VALUES (UUID_TO_BIN(?), ?, ?)",
                fixture.categoryId().toString(),
                "returns-it-" + fixture.categoryId(),
                "Returns rollback fixture");
        jdbc.update(
                """
                        INSERT INTO products (id, code, name, description, category_id, composite, active)
                        VALUES (UUID_TO_BIN(?), ?, ?, ?, UUID_TO_BIN(?), FALSE, TRUE)
                        """,
                fixture.productId().toString(), "RET-" + fixture.productId(),
                "Return rollback product", "Returns rollback fixture",
                fixture.categoryId().toString());
        jdbc.update(
                """
                        INSERT INTO customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, created_at, updated_at)
                        VALUES (UUID_TO_BIN(?), ?, ?, 'INDIVIDUAL', ?, ?, 0, 0, TRUE, ?, ?)
                        """,
                fixture.customerId().toString(), fixture.code(),
                "Returns rollback customer", "RIF-" + fixture.customerId(),
                "Returns rollback", now, now);
        jdbc.update("""
                INSERT INTO warehouses (id, code, name, address, active)
                VALUES (UUID_TO_BIN(?), ?, ?, ?, TRUE)
                """, fixture.warehouseId().toString(),
                "RET-" + fixture.warehouseId(), "Returns rollback warehouse",
                "Integration test");
        jdbc.update(
                """
                        INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity, reserved, last_movement_at, updated_at)
                        VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 10, 0, ?, ?)
                        """,
                UUID.randomUUID().toString(), fixture.warehouseId().toString(),
                fixture.productId().toString(), now, now);
        jdbc.update(
                """
                        INSERT INTO invoices (id, invoice_number, customer_id, customer_name, customer_rif,
                            created_by, created_by_name, status, payment_type, subtotal, returned_total, issue_date, due_date)
                        VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), ?, ?, UUID_TO_BIN(?), ?, 'PAID', 'CREDIT', 100.00, 0.00, ?, ?)
                        """,
                fixture.invoiceId().toString(),
                shortNumber("INV", fixture.invoiceId()),
                fixture.customerId().toString(), "Returns rollback customer",
                "RIF-" + fixture.customerId(), fixture.actorId().toString(),
                "Rollback Operator", LocalDate.now(),
                LocalDate.now().plusDays(30));
        jdbc.update(
                """
                        INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity,
                            returned_quantity, unit_price, line_total)
                        VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), ?, ?, 2.0000, 0.0000, 50.00, 100.00)
                        """,
                fixture.invoiceLineId().toString(),
                fixture.invoiceId().toString(), fixture.productId().toString(),
                "Return rollback product", "RET-" + fixture.productId());
        jdbc.update(
                """
                        INSERT INTO payments (id, payment_number, customer_id, amount, payment_date, status,
                            applied_amount, created_by, created_by_name)
                        VALUES (UUID_TO_BIN(?), ?, UUID_TO_BIN(?), 100.00, ?, 'APPLIED', 100.00, UUID_TO_BIN(?), ?)
                        """,
                fixture.paymentId().toString(),
                shortNumber("PAY", fixture.paymentId()),
                fixture.customerId().toString(), LocalDate.now(),
                fixture.actorId().toString(), "Rollback Operator");
        jdbc.update(
                """
                        INSERT INTO payment_allocations (id, payment_id, invoice_id, allocated_amount)
                        VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), UUID_TO_BIN(?), 100.00)
                        """,
                UUID.randomUUID().toString(), fixture.paymentId().toString(),
                fixture.invoiceId().toString());
        jdbc.update("""
                INSERT INTO customer_balances (id, customer_id, total_balance)
                VALUES (UUID_TO_BIN(?), UUID_TO_BIN(?), 10.00)
                """, UUID.randomUUID().toString(),
                fixture.customerId().toString());
        jdbc.update(
                """
                        INSERT INTO cash_registers (id, closure_date, status, opened_by, opened_by_name, opened_at,
                            opening_balance, created_at, updated_at)
                        VALUES (UUID_TO_BIN(?), ?, 'OPEN', UUID_TO_BIN(?), ?, ?, 0.00, ?, ?)
                        """,
                fixture.registerId().toString(), LocalDate.now(),
                fixture.actorId().toString(), "Rollback Operator", now, now,
                now);
        return fixture;
    }

    private void failAfterRealOperation(FailureStage stage) {
        switch (stage) {
            case CASH -> doAnswer(invocation -> {
                invocation.callRealMethod();
                throw new InjectedFailure();
            }).when(cash).record(any(ReturnCashGateway.Entry.class));
            case STOCK -> doAnswer(invocation -> {
                invocation.callRealMethod();
                throw new InjectedFailure();
            }).when(inventory)
                    .restoreStock(any(ReturnInventoryGateway.Command.class));
            case INVOICE -> doAnswer(invocation -> {
                invocation.callRealMethod();
                throw new InjectedFailure();
            }).when(invoiceAdjustments).apply(any(Return.class));
            case BALANCE -> doAnswer(invocation -> {
                invocation.callRealMethod();
                throw new InjectedFailure();
            }).when(balances).credit(any(ReturnBalanceGateway.Command.class));
        }
    }

    private void assertDatabaseStateIsUnchanged(Fixture fixture) {
        assertEquals(0, count(
                "SELECT COUNT(*) FROM returns WHERE invoice_id = UUID_TO_BIN(?)",
                fixture.invoiceId()));
        assertEquals(0, count("SELECT COUNT(*) FROM return_number_sequence"));
        assertEquals(0, count(
                "SELECT COUNT(*) FROM cash_register_movements WHERE register_id = UUID_TO_BIN(?)",
                fixture.registerId()));
        assertEquals(10, scalar(
                "SELECT quantity FROM warehouse_stock WHERE warehouse_id = UUID_TO_BIN(?) AND product_id = UUID_TO_BIN(?)",
                fixture.warehouseId(), fixture.productId()));
        assertEquals(0, count(
                "SELECT COUNT(*) FROM stock_movements WHERE warehouse_id = UUID_TO_BIN(?) AND product_id = UUID_TO_BIN(?) AND movement_type = 'RETURN_ENTRY'",
                fixture.warehouseId(), fixture.productId()));
        assertEquals(0, decimal(
                "SELECT returned_total FROM invoices WHERE id = UUID_TO_BIN(?)",
                fixture.invoiceId()).compareTo(BigDecimal.ZERO));
        assertEquals(0, decimal(
                "SELECT returned_quantity FROM invoice_lines WHERE id = UUID_TO_BIN(?)",
                fixture.invoiceLineId()).compareTo(BigDecimal.ZERO));
        assertEquals(1, count(
                "SELECT COUNT(*) FROM payment_allocations WHERE invoice_id = UUID_TO_BIN(?)",
                fixture.invoiceId()));
        assertEquals("APPLIED",
                jdbc.queryForObject(
                        "SELECT status FROM payments WHERE id = UUID_TO_BIN(?)",
                        String.class, fixture.paymentId().toString()));
        assertEquals(0, decimal(
                "SELECT applied_amount FROM payments WHERE id = UUID_TO_BIN(?)",
                fixture.paymentId()).compareTo(new BigDecimal("100.00")));
        assertEquals(0, decimal(
                "SELECT total_balance FROM customer_balances WHERE customer_id = UUID_TO_BIN(?)",
                fixture.customerId()).compareTo(new BigDecimal("10.00")));
        assertEquals(0, count(
                "SELECT COUNT(*) FROM balance_transactions WHERE customer_id = UUID_TO_BIN(?)",
                fixture.customerId()));
    }

    private int count(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, Integer.class,
                jdbcParameters(parameters));
    }

    private int scalar(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, Integer.class,
                jdbcParameters(parameters));
    }

    private BigDecimal decimal(String sql, Object... parameters) {
        return jdbc.queryForObject(sql, BigDecimal.class,
                jdbcParameters(parameters));
    }

    private Object[] jdbcParameters(Object... parameters) {
        return java.util.Arrays.stream(parameters)
                .map(parameter -> parameter instanceof UUID id
                        ? id.toString()
                        : parameter)
                .toArray();
    }

    private CreateReturnRequest request(Fixture fixture) {
        return new CreateReturnRequest(fixture.invoiceId(),
                fixture.customerId(), "MANUAL", "Rollback scenario",
                List.of(new CreateReturnRequest.ReturnLineRequest(
                        fixture.invoiceLineId(), fixture.productId(),
                        BigDecimal.ONE, new BigDecimal("40.00"),
                        new BigDecimal("40.00"), null,
                        List.of(new CreateReturnRequest.ReturnLineRequest.AllocationRequest(
                                fixture.warehouseId(), BigDecimal.ONE)))));
    }

    private String shortNumber(String prefix, UUID id) {
        return prefix + "-" + id.toString().replace("-", "").substring(0, 16);
    }

    private static String requiredProperty(String property,
            String environmentVariable) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environmentVariable);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Provide -D" + property + " or "
                    + environmentVariable + " for an isolated MySQL database");
        }
        return value;
    }

    private enum FailureStage {
        CASH, STOCK, INVOICE, BALANCE
    }

    private record Fixture(UUID actorId, UUID categoryId, UUID productId,
            UUID customerId, UUID warehouseId, UUID invoiceId,
            UUID invoiceLineId, UUID paymentId, UUID registerId, long code) {
        private static Fixture create() {
            return new Fixture(UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), System.nanoTime());
        }
    }

    private static final class InjectedFailure extends RuntimeException {
    }
}
