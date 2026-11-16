package com.cashly.cashly_api.partners.customers.warehouse_access.application.architecture;

import com.cashly.cashly_api.partners.customers.profile.application.register.use_case.RegisterCustomerUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.grant.use_case.GrantWarehouseAccessUseCase;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.list.use_case.ListCustomerWarehousesUseCase;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CustomerWarehouseBoundaryTest {
    @Test
    void should_resolve_warehouses_through_customer_owned_contract() {
        for (Class<?> useCase : new Class<?>[]{
                GrantWarehouseAccessUseCase.class,
                ListCustomerWarehousesUseCase.class,
                RegisterCustomerUseCase.class}) {
            var dependencies = Arrays
                    .stream(useCase.getConstructors()[0].getParameterTypes())
                    .map(Class::getName).toList();
            assertTrue(dependencies.contains(
                    "com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway"),
                    useCase.getSimpleName() + dependencies);
            assertTrue(dependencies.stream().noneMatch(type -> type.startsWith(
                    "com.cashly.cashly_api.inventory.warehouses.")));
        }
    }

    @Test
    void should_keep_customer_application_free_of_foreign_module_types()
            throws Exception {
        Path customers = Path
                .of("src/main/java/com/cashly/cashly_api/partners/customers");
        try (var files = Files.walk(customers)) {
            var applicationSources = files
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> customers.relativize(path).toString()
                            .replace('\\', '/').contains("/application/"))
                    .toList();
            assertFalse(applicationSources.isEmpty(),
                    "Customer application source scan must not be empty");
            var foreignImports = applicationSources.stream().flatMap(path -> {
                try {
                    return Files.readAllLines(path).stream();
                } catch (java.io.IOException exception) {
                    throw new java.io.UncheckedIOException(exception);
                }
            }).filter(line -> line
                    .startsWith("import com.cashly.cashly_api.platform.auth.")
                    || line.startsWith(
                            "import com.cashly.cashly_api.finance.treasury.cash_register.")
                    || line.startsWith(
                            "import com.cashly.cashly_api.inventory.warehouses."))
                    .toList();
            assertTrue(foreignImports.isEmpty(), foreignImports.toString());
        }
    }

}
