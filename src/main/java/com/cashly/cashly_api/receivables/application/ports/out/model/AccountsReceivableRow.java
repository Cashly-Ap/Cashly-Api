package com.cashly.cashly_api.receivables.application.ports.out.model;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountsReceivableRow(UUID customerId, String customerName, String rif,
                                    BigDecimal creditLimit, BigDecimal pendingBalance,
                                    BigDecimal availableCredit, BigDecimal age0_30,
                                    BigDecimal age31_60, BigDecimal age61_90,
                                    BigDecimal age91_120, BigDecimal age120Plus) {}
