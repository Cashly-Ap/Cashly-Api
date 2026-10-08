package com.cashly.cashly_api.receivables.application.api;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountsReceivableReportSnapshot(UUID customerId, String customerName, String rif,
                                    BigDecimal creditLimit, BigDecimal pendingBalance,
                                    BigDecimal availableCredit, BigDecimal age0_30,
                                    BigDecimal age31_60, BigDecimal age61_90,
                                    BigDecimal age91_120, BigDecimal age120Plus) {}
