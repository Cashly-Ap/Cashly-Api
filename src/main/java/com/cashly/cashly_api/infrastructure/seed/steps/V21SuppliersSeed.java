package com.cashly.cashly_api.infrastructure.seed.steps;

import static com.cashly.cashly_api.infrastructure.seed.SeedIds.id;

import com.cashly.cashly_api.infrastructure.seed.SeedStep;
import com.cashly.cashly_api.partners.suppliers.infrastructure.persistence.entities.SupplierAddressEntity;
import com.cashly.cashly_api.partners.suppliers.infrastructure.persistence.entities.SupplierEmailEntity;
import com.cashly.cashly_api.partners.suppliers.infrastructure.persistence.entities.SupplierEntity;
import com.cashly.cashly_api.partners.suppliers.infrastructure.persistence.entities.SupplierPhoneEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class V21SuppliersSeed implements SeedStep {
    private final EntityManager entityManager;
    public V21SuppliersSeed(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public int version() {
        return 21;
    }
    @Override
    public void seed() {
        supplier("one", 1L, "Coffee Growers, C.A.", "Coffee Growers",
                "J-20000001-1", "coffee@example.test", "+58-212-555-0201");
        supplier("two", 2L, "Spring Water, C.A.", "Spring Water",
                "J-20000002-2", "water@example.test", "+58-212-555-0202");
    }
    private void supplier(String key, long code, String legalName, String alias,
            String rif, String emailValue, String phoneValue) {
        var supplierId = id("supplier." + key);
        if (entityManager.find(SupplierEntity.class, supplierId) != null)
            return;
        var supplier = new SupplierEntity();
        supplier.setId(supplierId);
        supplier.setCode(code);
        supplier.setLegalName(legalName);
        supplier.setAlias(alias);
        supplier.setType("NATIONAL");
        supplier.setRif(rif);
        supplier.setNotes("Deterministic seed supplier");
        supplier.setActive(true);
        supplier.setCreatedAt(LocalDateTime.of(2025, 1, 13, 8, 0));
        supplier.setUpdatedAt(LocalDateTime.of(2025, 1, 13, 8, 0));
        var email = new SupplierEmailEntity();
        email.setId(id("supplier-email." + key));
        email.setSupplierId(supplierId);
        email.setEmail(emailValue);
        supplier.getEmails().add(email);
        var phone = new SupplierPhoneEntity();
        phone.setId(id("supplier-phone." + key));
        phone.setSupplierId(supplierId);
        phone.setPhone(phoneValue);
        supplier.getPhones().add(phone);
        var address = new SupplierAddressEntity();
        address.setId(id("supplier-address." + key));
        address.setSupplierId(supplierId);
        address.setAddress("Seed Avenue " + code);
        address.setBilling(true);
        address.setActive(true);
        address.setState("Miranda");
        address.setMunicipality("Chacao");
        address.setPostalCode("1060");
        supplier.getAddresses().add(address);
        entityManager.persist(supplier);
    }
}
