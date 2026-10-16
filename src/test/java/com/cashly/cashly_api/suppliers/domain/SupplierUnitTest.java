package com.cashly.cashly_api.suppliers.domain;

import com.cashly.cashly_api.suppliers.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.suppliers.domain.valueobjects.LegalName;
import com.cashly.cashly_api.suppliers.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.suppliers.domain.valueobjects.Rif;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierAlias;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierCode;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierId;
import com.cashly.cashly_api.suppliers.domain.valueobjects.SupplierType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SupplierUnitTest {

    @Test
    void should_create_active_supplier_with_empty_collections() {
        Supplier supplier = createTestSupplier();

        assertNotNull(supplier.getId());
        assertNotNull(supplier.getCode());
        assertTrue(supplier.isActive());
        assertNotNull(supplier.getCreatedAt());
        assertNotNull(supplier.getUpdatedAt());
        assertTrue(supplier.getEmails().isEmpty());
        assertTrue(supplier.getPhones().isEmpty());
        assertTrue(supplier.getAddresses().isEmpty());
    }

    @Test
    void should_reconstitute_supplier_with_full_state() {
        SupplierId id = SupplierId.generate();
        LocalDateTime now = LocalDateTime.now();
        List<SupplierEmail> emails = List.of(
            SupplierEmail.create(id, new EmailAddress("supplier@example.com"))
        );
        List<SupplierPhone> phones = List.of(
            SupplierPhone.create(id, new PhoneNumber("+58-212-555-1234"))
        );
        List<SupplierAddress> addresses = List.of(
            SupplierAddress.create(id, "123 Main St", true, true, "State", "Mun", "1000")
        );

        Supplier supplier = Supplier.reconstitute(
            id,
            SupplierCode.from(100L),
            new LegalName("Proveedor C.A."),
            SupplierType.JURIDICO,
            new Rif("J-12345678-9"),
            new SupplierAlias("Proveedor"),
            "Some notes",
            false,
            now,
            now,
            emails,
            phones,
            addresses
        );

        assertEquals(id, supplier.getId());
        assertEquals(100L, supplier.getCode().getValue());
        assertEquals("Proveedor C.A.", supplier.getLegalName().getValue());
        assertEquals(SupplierType.JURIDICO, supplier.getType());
        assertEquals("J-12345678-9", supplier.getRif().getValue());
        assertEquals("Proveedor", supplier.getAlias().getValue());
        assertEquals("Some notes", supplier.getNotes());
        assertFalse(supplier.isActive());
        assertEquals(now, supplier.getCreatedAt());
        assertEquals(now, supplier.getUpdatedAt());
        assertEquals(1, supplier.getEmails().size());
        assertEquals(1, supplier.getPhones().size());
        assertEquals(1, supplier.getAddresses().size());
    }

    @Test
    void should_update_legal_name() {
        Supplier supplier = createTestSupplier();
        LocalDateTime before = supplier.getUpdatedAt();

        supplier.updateLegalName(new LegalName("New Name C.A."));
        assertEquals("New Name C.A.", supplier.getLegalName().getValue());
        assertTrue(supplier.getUpdatedAt().isAfter(before) || supplier.getUpdatedAt().equals(before));
    }

    @Test
    void should_throw_on_null_legal_name() {
        Supplier supplier = createTestSupplier();
        assertThrows(IllegalArgumentException.class, () -> supplier.updateLegalName(null));
    }

    @Test
    void should_update_rif() {
        Supplier supplier = createTestSupplier();
        LocalDateTime before = supplier.getUpdatedAt();

        supplier.updateRif(new Rif("V-87654321-1"));
        assertEquals("V-87654321-1", supplier.getRif().getValue());
        assertTrue(supplier.getUpdatedAt().isAfter(before) || supplier.getUpdatedAt().equals(before));
    }

    @Test
    void should_throw_on_null_rif() {
        Supplier supplier = createTestSupplier();
        assertThrows(IllegalArgumentException.class, () -> supplier.updateRif(null));
    }

    @Test
    void should_update_alias() {
        Supplier supplier = createTestSupplier();
        LocalDateTime before = supplier.getUpdatedAt();

        supplier.updateAlias(new SupplierAlias("NewAlias"));
        assertEquals("NewAlias", supplier.getAlias().getValue());
        assertTrue(supplier.getUpdatedAt().isAfter(before) || supplier.getUpdatedAt().equals(before));
    }

    @Test
    void should_throw_on_null_alias() {
        Supplier supplier = createTestSupplier();
        assertThrows(IllegalArgumentException.class, () -> supplier.updateAlias(null));
    }

    @Test
    void should_update_type() {
        Supplier supplier = createTestSupplier();
        LocalDateTime before = supplier.getUpdatedAt();

        supplier.updateType(SupplierType.NATURAL);
        assertEquals(SupplierType.NATURAL, supplier.getType());
        assertTrue(supplier.getUpdatedAt().isAfter(before) || supplier.getUpdatedAt().equals(before));
    }

    @Test
    void should_throw_on_null_type() {
        Supplier supplier = createTestSupplier();
        assertThrows(IllegalArgumentException.class, () -> supplier.updateType(null));
    }

    @Test
    void should_update_notes() {
        Supplier supplier = createTestSupplier();
        LocalDateTime before = supplier.getUpdatedAt();

        supplier.updateNotes("Important notes");
        assertEquals("Important notes", supplier.getNotes());
        assertTrue(supplier.getUpdatedAt().isAfter(before) || supplier.getUpdatedAt().equals(before));
    }

    @Test
    void should_activate_supplier() {
        Supplier supplier = createTestSupplier();
        supplier.deactivate();
        assertFalse(supplier.isActive());

        supplier.activate();
        assertTrue(supplier.isActive());
        assertNotNull(supplier.getUpdatedAt());
    }

    @Test
    void should_deactivate_supplier() {
        Supplier supplier = createTestSupplier();
        assertTrue(supplier.isActive());

        supplier.deactivate();
        assertFalse(supplier.isActive());
        assertNotNull(supplier.getUpdatedAt());
    }

    @Test
    void should_add_email_to_collection() {
        Supplier supplier = createTestSupplier();
        assertTrue(supplier.getEmails().isEmpty());

        supplier.addEmail(new EmailAddress("supplier@example.com"));
        assertEquals(1, supplier.getEmails().size());
        assertEquals("supplier@example.com", supplier.getEmails().get(0).getEmail().getValue());
    }

    @Test
    void should_add_phone_to_collection() {
        Supplier supplier = createTestSupplier();
        assertTrue(supplier.getPhones().isEmpty());

        supplier.addPhone(new PhoneNumber("+58-212-555-1234"));
        assertEquals(1, supplier.getPhones().size());
        assertEquals("+58-212-555-1234", supplier.getPhones().get(0).getPhone().getValue());
    }

    @Test
    void should_add_address_to_collection() {
        Supplier supplier = createTestSupplier();
        assertTrue(supplier.getAddresses().isEmpty());

        supplier.addAddress("123 Main St", true, true, "State", "Municipality", "1000");
        assertEquals(1, supplier.getAddresses().size());
        assertEquals("123 Main St", supplier.getAddresses().get(0).getAddress());
        assertTrue(supplier.getAddresses().get(0).isBilling());
    }

    @Test
    void should_clear_other_billing_addresses_when_adding_billing() {
        Supplier supplier = createTestSupplier();
        supplier.addAddress("Address 1", true, true, "State", "Mun", "1000");
        supplier.addAddress("Address 2", true, true, "State", "Mun", "1000");

        assertFalse(supplier.getAddresses().get(0).isBilling());
        assertTrue(supplier.getAddresses().get(1).isBilling());
    }

    @Test
    void should_clear_emails() {
        Supplier supplier = createTestSupplier();
        supplier.addEmail(new EmailAddress("supplier@example.com"));
        assertEquals(1, supplier.getEmails().size());

        supplier.clearEmails();
        assertTrue(supplier.getEmails().isEmpty());
    }

    @Test
    void should_clear_phones() {
        Supplier supplier = createTestSupplier();
        supplier.addPhone(new PhoneNumber("5551234"));
        assertEquals(1, supplier.getPhones().size());

        supplier.clearPhones();
        assertTrue(supplier.getPhones().isEmpty());
    }

    @Test
    void should_clear_addresses() {
        Supplier supplier = createTestSupplier();
        supplier.addAddress("123 Main St", false, true, "State", "Mun", "1000");
        assertEquals(1, supplier.getAddresses().size());

        supplier.clearAddresses();
        assertTrue(supplier.getAddresses().isEmpty());
    }

    @Test
    void should_be_equal_by_id_only() {
        SupplierId id = SupplierId.generate();
        LocalDateTime now = LocalDateTime.now();

        Supplier supplier1 = Supplier.reconstitute(
            id, SupplierCode.from(1L), new LegalName("Name 1"), SupplierType.JURIDICO,
            new Rif("J-11111111-1"), new SupplierAlias("Alias1"),
            null, true, now, now, List.of(), List.of(), List.of()
        );

        Supplier supplier2 = Supplier.reconstitute(
            id, SupplierCode.from(2L), new LegalName("Name 2"), SupplierType.NATURAL,
            new Rif("J-22222222-2"), new SupplierAlias("Alias2"),
            null, false, now, now, List.of(), List.of(), List.of()
        );

        assertEquals(supplier1, supplier2);
        assertEquals(supplier1.hashCode(), supplier2.hashCode());
    }

    @Test
    void should_not_be_equal_for_different_ids() {
        Supplier supplier1 = createTestSupplier();
        Supplier supplier2 = createTestSupplier();
        assertNotEquals(supplier1, supplier2);
    }

    @Test
    void should_return_unmodifiable_emails() {
        Supplier supplier = createTestSupplier();
        assertThrows(UnsupportedOperationException.class, () -> supplier.getEmails().add(null));
    }

    @Test
    void should_return_unmodifiable_phones() {
        Supplier supplier = createTestSupplier();
        assertThrows(UnsupportedOperationException.class, () -> supplier.getPhones().add(null));
    }

    @Test
    void should_return_unmodifiable_addresses() {
        Supplier supplier = createTestSupplier();
        assertThrows(UnsupportedOperationException.class, () -> supplier.getAddresses().add(null));
    }

    @Test
    void should_return_id_and_code_and_createdAt_as_immutable() {
        Supplier supplier = createTestSupplier();

        SupplierId originalId = supplier.getId();
        SupplierCode originalCode = supplier.getCode();
        LocalDateTime originalCreatedAt = supplier.getCreatedAt();

        supplier.updateLegalName(new LegalName("New Name"));

        assertSame(originalId, supplier.getId());
        assertSame(originalCode, supplier.getCode());
        assertSame(originalCreatedAt, supplier.getCreatedAt());
    }

    private Supplier createTestSupplier() {
        return Supplier.create(
            SupplierCode.from(1L),
            new LegalName("Proveedor C.A."),
            SupplierType.JURIDICO,
            new Rif("J-12345678-9"),
            new SupplierAlias("Proveedor")
        );
    }
}
