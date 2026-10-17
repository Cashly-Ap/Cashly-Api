package com.cashly.cashly_api.finance.billing.returns.application.architecture;

import com.cashly.cashly_api.finance.billing.returns.application.create.use_case.CreateReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.delete.use_case.DeleteReturnUseCase;
import com.cashly.cashly_api.finance.billing.returns.application.services.ReturnResponseAssembler;
import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
class ReturnFinancialBoundaryTest {
    @ParameterizedTest
    @ValueSource(classes = {CreateReturnUseCase.class, DeleteReturnUseCase.class, ReturnResponseAssembler.class})
    void should_depend_only_on_return_owned_contracts(Class<?> component) {
        var dependencies = Arrays.stream(component.getConstructors()[0].getParameterTypes()).map(Class::getName).toList();
        assertTrue(dependencies.stream().allMatch(type -> type.startsWith("com.cashly.cashly_api.finance.billing.returns.")), component.getSimpleName() + dependencies);
    }
}
