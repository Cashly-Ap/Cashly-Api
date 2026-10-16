package com.cashly.cashly_api.returns.application.architecture;

import com.cashly.cashly_api.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.returns.application.delete.use_case.DeleteReturnUseCase;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReturnInventoryBoundaryTest {
    @Test
    void shouldRejectWarehouseDependenciesInApplicationConstructors() {
        for (Class<?> useCase : List.of(CreateReturnUseCase.class, DeleteReturnUseCase.class)) {
            for (var constructor : useCase.getConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getPackageName().startsWith("com.cashly.cashly_api.warehouses"), parameter.getName());
                }
            }
        }
    }
}
