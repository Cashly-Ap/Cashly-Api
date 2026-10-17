package com.cashly.cashly_api.finance.billing.returns.domain.architecture;

import com.cashly.cashly_api.finance.billing.returns.domain.entity.Return;


import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;

class ReturnInvoiceReferenceBoundaryTest {
    @Test
    void should_store_invoice_identity_without_importing_the_invoice_domain() {
        assertThat(Arrays.stream(Return.class.getDeclaredFields()).map(field -> field.getType().getPackageName()))
                .noneMatch(name -> name.startsWith("com.cashly.cashly_api.finance.billing.invoices"));
    }
}
