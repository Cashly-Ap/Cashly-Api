package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.*;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.BalanceTransactionEntity;
import com.cashly.cashly_api.partners.customers.balance.infrastructure.persistence.entities.CustomerBalanceEntity;
import com.cashly.cashly_api.partners.customers.profile.infrastructure.persistence.entities.CustomerAddressEntity;
import com.cashly.cashly_api.partners.customers.profile.infrastructure.persistence.entities.CustomerEmailEntity;
import com.cashly.cashly_api.partners.customers.profile.infrastructure.persistence.entities.CustomerEntity;
import com.cashly.cashly_api.partners.customers.profile.infrastructure.persistence.entities.CustomerPhoneEntity;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class V06CustomersSeed implements SeedStep {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 3,
            8, 0);
    private final EntityManager entityManager;

    public V06CustomersSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public int version() {
        return 6;
    }

    @Override
    public void seed() {
        persistCustomer(CUSTOMER_ONE, 1001L, "Mercado Central, C.A.",
                "BUSINESS", "J-10000001-1", "Mercado Central",
                "compras@mercadocentral.local", "+58-212-1000001",
                "Avenida Mercado 1");
        persistCustomer(CUSTOMER_TWO, 1002L, "Ana Pérez", "INDIVIDUAL",
                "V-10000002-2", "Ana", "ana.perez@example.local",
                "+58-412-1000002", "Calle Los Cedros 2");
        persistBalance(CUSTOMER_ONE, new BigDecimal("120.00"));
        persistBalance(CUSTOMER_TWO, new BigDecimal("40.00"));
        persistTransaction(CUSTOMER_ONE, new BigDecimal("120.00"), "BAL-0001");
        persistTransaction(CUSTOMER_TWO, new BigDecimal("40.00"), "BAL-0002");
    }

    private void persistCustomer(UUID id, long code, String legalName,
            String type, String rif, String alias, String emailValue,
            String phoneValue, String addressValue) {
        if (entityManager.find(CustomerEntity.class, id) != null)
            return;
        var customer = new CustomerEntity();
        customer.setId(id);
        customer.setCode(code);
        customer.setLegalName(legalName);
        customer.setType(type);
        customer.setRif(rif);
        customer.setAlias(alias);
        customer.setCreditDays(30);
        customer.setCreditLimit(new BigDecimal("1000.00"));
        customer.setActive(true);
        customer.setCreatedAt(CREATED_AT);
        customer.setUpdatedAt(CREATED_AT);
        customer.setNotes("Deterministic seed customer");
        customer.setPriceTypeId(id("price-type.base"));
        var email = new CustomerEmailEntity();
        email.setId(id("customer-email." + code));
        email.setEmail(emailValue);
        var phone = new CustomerPhoneEntity();
        phone.setId(id("customer-phone." + code));
        phone.setPhone(phoneValue);
        var address = new CustomerAddressEntity();
        address.setId(id("customer-address." + code));
        address.setAddress(addressValue);
        address.setBilling(true);
        address.setActive(true);
        address.setState("Distrito Capital");
        address.setMunicipality("Libertador");
        address.setPostalCode("1010");
        customer.getEmails().add(email);
        customer.getPhones().add(phone);
        customer.getAddresses().add(address);
        entityManager.persist(customer);
    }

    private void persistBalance(UUID customerId, BigDecimal amount) {
        UUID balanceId = id("customer-balance." + customerId);
        if (entityManager.find(CustomerBalanceEntity.class, balanceId) != null)
            return;
        var balance = new CustomerBalanceEntity();
        balance.setId(balanceId);
        balance.setCustomerId(customerId);
        balance.setTotalBalance(amount);
        balance.setCreatedAt(CREATED_AT);
        balance.setUpdatedAt(CREATED_AT);
        entityManager.persist(balance);
    }

    private void persistTransaction(UUID customerId, BigDecimal amount,
            String number) {
        UUID transactionId = id("balance-transaction." + customerId);
        if (entityManager.find(BalanceTransactionEntity.class,
                transactionId) != null)
            return;
        var transaction = new BalanceTransactionEntity();
        transaction.setId(transactionId);
        transaction.setCustomerId(customerId);
        transaction.setType("DEBIT");
        transaction.setAmount(amount);
        transaction.setReferenceType("SEED");
        transaction.setNumber(number);
        transaction.setReferenceNumber(number);
        transaction.setCreatedBy(ADMIN_USER);
        transaction.setCreatedAt(CREATED_AT);
        entityManager.persist(transaction);
    }
}
