package com.cashly.cashly_api.finance.billing.invoices.infrastructure.integration_tests;

import com.cashly.cashly_api.CashlyApplication;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.use_case.ConvertQuoteToInvoiceUseCase;
import com.cashly.cashly_api.sales.quotes.application.convert_to_invoice.request.ConvertQuoteToInvoiceRequest;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.use_case.ConvertToInvoiceUseCase;
import com.cashly.cashly_api.sales.orders.application.convert_to_invoice.request.ConvertToInvoiceRequest;
import com.cashly.cashly_api.finance.billing.invoices.application.ports.out.StockDeductionGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

/** Real conversion, invoice authorization, payments, balance, stock and database foreign keys. */
@SpringBootTest(classes = CashlyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@ContextConfiguration(initializers = CashInvoiceConversionMySqlIT.IsolatedDatabaseOnly.class)
@Transactional
class CashInvoiceConversionMySqlIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired ConvertQuoteToInvoiceUseCase quoteConversion;
    @Autowired ConvertToInvoiceUseCase orderConversion;
    @MockitoSpyBean StockDeductionGateway stock;

    public static class IsolatedDatabaseOnly implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext context) {
            if (!"jdbc:mysql://suite-db:3306/screaming_suite".equals(context.getEnvironment().getProperty("spring.datasource.url"))) {
                throw new IllegalStateException("CashInvoiceConversionMySqlIT requires the isolated verification database");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 30, 100})
    void convertsQuoteToCashInvoiceWithPaymentBalanceOrBoth(int balance) {
        Fixture f = seed();
        var converted = quoteConversion.execute(f.quote(), new ConvertQuoteToInvoiceRequest(
                "CASH", null, balance > 0, BigDecimal.valueOf(balance), balance < 100 ? f.method() : null), f.actor());
        entityManager.flush();
        assertThat(text("select status from quotes where id=UUID_TO_BIN(?)", f.quote())).isEqualTo("CONVERTED_TO_INVOICE");
        assertThat(text("select status from invoices where id=UUID_TO_BIN(?)", converted.invoiceId())).isEqualTo("PAID");
        assertThat(decimal("select total_balance from customer_balances where customer_id=UUID_TO_BIN(?)", f.customer())).isEqualByComparingTo(BigDecimal.valueOf(100 - balance));
        assertThat(decimal("select coalesce(sum(allocated_amount),0) from payment_allocations where invoice_id=UUID_TO_BIN(?)", converted.invoiceId())).isEqualByComparingTo(BigDecimal.valueOf(100 - balance));
        assertThat(count("select quantity from warehouse_stock where warehouse_id=UUID_TO_BIN(?)", f.warehouse())).isEqualTo(8);
    }

    @Test
    void convertsConfirmedOrderToCashInvoiceWithRealPaymentAllocation() {
        Fixture f = seed();
        var converted = orderConversion.execute(f.order(), new ConvertToInvoiceRequest("CASH", null, false, null, f.method()), f.actor());
        entityManager.flush();
        assertThat(text("select status from orders where id=UUID_TO_BIN(?)", f.order())).isEqualTo("INVOICED");
        assertThat(text("select status from invoices where id=UUID_TO_BIN(?)", converted.invoiceId())).isEqualTo("PAID");
        assertThat(decimal("select sum(allocated_amount) from payment_allocations where invoice_id=UUID_TO_BIN(?)", converted.invoiceId())).isEqualByComparingTo("100.00");
    }

    @Test
    void rollsBackInvoicePaymentBalanceCashAndStockWhenConversionFailsAfterDeduction() {
        Fixture f = seed();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        try {
            doAnswer(invocation -> { invocation.callRealMethod(); throw new InjectedFailure(); }).when(stock).deduct(anyList());
            assertThatThrownBy(() -> quoteConversion.execute(f.quote(), new ConvertQuoteToInvoiceRequest(
                    "CASH", null, true, new BigDecimal("30.00"), f.method()), f.actor())).isInstanceOf(InjectedFailure.class);
            assertThat(text("select status from quotes where id=UUID_TO_BIN(?)", f.quote())).isEqualTo("DRAFT");
            assertThat(count("select count(*) from invoices where customer_id=UUID_TO_BIN(?)", f.customer())).isZero();
            assertThat(count("select count(*) from payments where customer_id=UUID_TO_BIN(?)", f.customer())).isZero();
            assertThat(count("select count(*) from balance_transactions where customer_id=UUID_TO_BIN(?)", f.customer())).isZero();
            assertThat(count("select count(*) from cash_register_movements where register_id=UUID_TO_BIN(?)", f.register())).isZero();
            assertThat(count("select count(*) from stock_movements where warehouse_id=UUID_TO_BIN(?)", f.warehouse())).isZero();
            assertThat(count("select quantity from warehouse_stock where warehouse_id=UUID_TO_BIN(?)", f.warehouse())).isEqualTo(10);
            assertThat(decimal("select total_balance from customer_balances where customer_id=UUID_TO_BIN(?)", f.customer())).isEqualByComparingTo("100.00");
        } finally {
            cleanCommittedFixture(f);
        }
    }

    private Fixture seed() {
        Fixture f = new Fixture(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        jdbc.update("insert into users(id,email,user_name,password_hash,role,first_name,last_name,phone,role_id,department_id,active,email_verified,created_at,updated_at) values(UUID_TO_BIN(?),?,'Cash IT','unused','SUPER_ADMIN','Cash','Integration',NULL,UNHEX('90000000000000000000000000000001'),UNHEX('91000000000000000000000000000001'),true,true,NOW(),NOW())", f.actor().toString(), f.actor()+"@cash-it.test");
        jdbc.update("insert into categories(id,name,active) values(UUID_TO_BIN(?),?,true)", f.category().toString(), "Cash "+f.category());
        jdbc.update("insert into products(id,code,name,category_id,composite,active) values(UUID_TO_BIN(?),?,'Cash product',UUID_TO_BIN(?),false,true)", f.product().toString(), "CS-"+f.product(), f.category().toString());
        jdbc.update("insert into customers(id,code,legal_name,type,rif,alias,credit_days,credit_limit,active,created_at,updated_at) values(UUID_TO_BIN(?),?,'Cash customer','INDIVIDUAL',?,'Cash IT',0,1000,true,NOW(),NOW())", f.customer().toString(), System.nanoTime(), "J-%08d-1".formatted(Math.floorMod(f.customer().hashCode(), 100000000)));
        jdbc.update("insert into warehouses(id,code,name,address,active) values(UUID_TO_BIN(?),?,'Cash warehouse','Test',true)", f.warehouse().toString(), "CS-"+f.warehouse());
        jdbc.update("insert into warehouse_stock(id,warehouse_id,product_id,quantity,reserved,updated_at) values(UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),10,0,NOW())", UUID.randomUUID().toString(),f.warehouse().toString(),f.product().toString());
        jdbc.update("insert into payment_methods(id,code,name,is_active) values(UUID_TO_BIN(?),?,'Cash IT',true)",f.method().toString(),"CS"+f.method().toString().substring(0,8));
        jdbc.update("insert into customer_balances(id,customer_id,total_balance) values(UUID_TO_BIN(?),UUID_TO_BIN(?),100)",UUID.randomUUID().toString(),f.customer().toString());
        jdbc.update("insert into cash_registers(id,closure_date,status,opened_by,opened_by_name,opened_at,opening_balance,created_at,updated_at) values(UUID_TO_BIN(?),CURRENT_DATE,'OPEN',UUID_TO_BIN(?),'Cash IT',NOW(),0,NOW(),NOW())",f.register().toString(),f.actor().toString());
        jdbc.update("insert into quotes(id,quote_number,customer_id,customer_name,customer_rif,status,subtotal,created_at,updated_at) values(UUID_TO_BIN(?),'PRE-2099-9999',UUID_TO_BIN(?),'Cash customer','RIF','DRAFT',100,NOW(),NOW())",f.quote().toString(),f.customer().toString());
        UUID quoteLine=UUID.randomUUID();
        jdbc.update("insert into quote_lines(id,quote_id,product_id,product_name,product_code,quantity,unit_price,line_total) values(UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),'Cash product','SKU',2,50,100)",quoteLine.toString(),f.quote().toString(),f.product().toString());
        jdbc.update("insert into quote_line_category_snapshots(quote_line_id,category_id) values(UUID_TO_BIN(?),UUID_TO_BIN(?))",quoteLine.toString(),f.category().toString());
        jdbc.update("insert into quote_allocations(id,quote_line_id,quote_id,product_id,warehouse_id,warehouse_name,allocated_quantity) values(UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),'Cash warehouse',2)",UUID.randomUUID().toString(),quoteLine.toString(),f.quote().toString(),f.product().toString(),f.warehouse().toString());
        jdbc.update("insert into orders(id,order_number,customer_id,customer_name,customer_rif,status,subtotal,created_at,updated_at) values(UUID_TO_BIN(?),'PED-2099-9999',UUID_TO_BIN(?),'Cash customer','RIF','CONFIRMED',100,NOW(),NOW())",f.order().toString(),f.customer().toString());
        UUID orderLine=UUID.randomUUID();
        jdbc.update("insert into order_lines(id,order_id,product_id,product_name,product_code,quantity,unit_price,line_total) values(UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),'Cash product','SKU',2,50,100)",orderLine.toString(),f.order().toString(),f.product().toString());
        jdbc.update("insert into order_line_category_snapshots(order_line_id,category_id) values(UUID_TO_BIN(?),UUID_TO_BIN(?))",orderLine.toString(),f.category().toString());
        jdbc.update("insert into order_line_allocations(id,order_line_id,warehouse_id,allocated) values(UUID_TO_BIN(?),UUID_TO_BIN(?),UUID_TO_BIN(?),2)",UUID.randomUUID().toString(),orderLine.toString(),f.warehouse().toString());
        return f;
    }

    private void cleanCommittedFixture(Fixture f) {
        jdbc.update("delete from quote_allocations where quote_id=UUID_TO_BIN(?)",f.quote().toString());
        jdbc.update("delete s from quote_line_category_snapshots s join quote_lines l on l.id=s.quote_line_id where l.quote_id=UUID_TO_BIN(?)",f.quote().toString());
        jdbc.update("delete from quote_lines where quote_id=UUID_TO_BIN(?)",f.quote().toString());
        jdbc.update("delete from quotes where id=UUID_TO_BIN(?)",f.quote().toString());
        jdbc.update("delete a from order_line_allocations a join order_lines l on l.id=a.order_line_id where l.order_id=UUID_TO_BIN(?)",f.order().toString());
        jdbc.update("delete s from order_line_category_snapshots s join order_lines l on l.id=s.order_line_id where l.order_id=UUID_TO_BIN(?)",f.order().toString());
        jdbc.update("delete from order_lines where order_id=UUID_TO_BIN(?)",f.order().toString());
        jdbc.update("delete from orders where id=UUID_TO_BIN(?)",f.order().toString());
        jdbc.update("delete from cash_registers where id=UUID_TO_BIN(?)",f.register().toString());
        jdbc.update("delete from customer_balances where customer_id=UUID_TO_BIN(?)",f.customer().toString());
        jdbc.update("delete from warehouse_stock where warehouse_id=UUID_TO_BIN(?)",f.warehouse().toString());
        jdbc.update("delete from warehouses where id=UUID_TO_BIN(?)",f.warehouse().toString());
        jdbc.update("delete from products where id=UUID_TO_BIN(?)",f.product().toString());
        jdbc.update("delete from categories where id=UUID_TO_BIN(?)",f.category().toString());
        jdbc.update("delete from customers where id=UUID_TO_BIN(?)",f.customer().toString());
        jdbc.update("delete from payment_methods where id=UUID_TO_BIN(?)",f.method().toString());
        jdbc.update("delete from users where id=UUID_TO_BIN(?)",f.actor().toString());
    }

    private int count(String sql,UUID id) {return jdbc.queryForObject(sql,Integer.class,id.toString());}
    private String text(String sql,UUID id) {return jdbc.queryForObject(sql,String.class,id.toString());}
    private BigDecimal decimal(String sql,UUID id) {return jdbc.queryForObject(sql,BigDecimal.class,id.toString());}
    private record Fixture(UUID actor,UUID category,UUID product,UUID customer,UUID warehouse,UUID method,UUID register,UUID quote,UUID order) {}
    private static class InjectedFailure extends RuntimeException {}
}
