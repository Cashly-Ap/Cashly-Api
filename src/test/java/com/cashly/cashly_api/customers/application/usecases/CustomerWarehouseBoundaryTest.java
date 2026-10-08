package com.cashly.cashly_api.customers.application.usecases;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class CustomerWarehouseBoundaryTest {
    @Test
    void should_resolve_warehouses_through_customer_owned_contract() {
        for (Class<?> useCase : new Class<?>[]{GrantWarehouseAccessUseCase.class,
                ListCustomerWarehousesUseCase.class, RegisterCustomerUseCase.class}) {
            var dependencies = Arrays.stream(useCase.getConstructors()[0].getParameterTypes()).map(Class::getName).toList();
            assertTrue(dependencies.contains("com.cashly.cashly_api.customers.application.contracts.WarehouseDirectoryGateway"),
                    useCase.getSimpleName() + dependencies);
            assertTrue(dependencies.stream().noneMatch(type -> type.startsWith("com.cashly.cashly_api.warehouses.")));
        }
    }

    @Test
    void should_keep_customer_application_free_of_foreign_module_types() throws Exception {
        Path application = Path.of("src/main/java/com/cashly/cashly_api/customers/application");
        try (var files = Files.walk(application)) {
            var foreignImports = files.filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> {
                        try {
                            return Files.readAllLines(path).stream();
                        } catch (java.io.IOException exception) {
                            throw new java.io.UncheckedIOException(exception);
                        }
                    })
                    .filter(line -> line.matches("import com\\.cashly\\.cashly_api\\.(auth|cashregister|warehouses)\\..*;"))
                    .toList();
            assertTrue(foreignImports.isEmpty(), foreignImports.toString());
        }
    }

}
