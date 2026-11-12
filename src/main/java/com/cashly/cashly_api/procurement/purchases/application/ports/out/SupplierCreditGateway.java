package com.cashly.cashly_api.procurement.purchases.application.ports.out;

import java.math.BigDecimal;
import java.util.UUID;

public interface SupplierCreditGateway {

    boolean isCreditAvailable(UUID supplierId, BigDecimal amount);

    BigDecimal getAvailableCredit(UUID supplierId);

    BigDecimal getTotalDebt(UUID supplierId);

    boolean hasOverduePurchases(UUID supplierId);
}
