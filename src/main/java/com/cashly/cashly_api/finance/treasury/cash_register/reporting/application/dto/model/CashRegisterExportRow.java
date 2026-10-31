package com.cashly.cashly_api.finance.treasury.cash_register.reporting.application.dto.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CashRegisterExportRow(LocalDate emissionDate, String documentType,
        String documentNumber, BigDecimal totalOperation, BigDecimal efectivoBs,
        BigDecimal dolar, BigDecimal transfDolar, BigDecimal zelle,
        BigDecimal anticipoAplicado, BigDecimal notaCreditoAplicado,
        BigDecimal notaDebito, String customerName, String module,
        int sortOrder) {
    public static CashRegisterExportRow of(LocalDate emissionDate,
            String documentType, String documentNumber,
            BigDecimal totalOperation, BigDecimal efectivoBs, BigDecimal dolar,
            BigDecimal transfDolar, BigDecimal zelle,
            BigDecimal anticipoAplicado, BigDecimal notaCreditoAplicado,
            BigDecimal notaDebito, String customerName, String module,
            int sortOrder) {
        return new CashRegisterExportRow(emissionDate, documentType,
                documentNumber, totalOperation,
                efectivoBs != null ? efectivoBs : BigDecimal.ZERO,
                dolar != null ? dolar : BigDecimal.ZERO,
                transfDolar != null ? transfDolar : BigDecimal.ZERO,
                zelle != null ? zelle : BigDecimal.ZERO,
                anticipoAplicado != null ? anticipoAplicado : BigDecimal.ZERO,
                notaCreditoAplicado != null
                        ? notaCreditoAplicado
                        : BigDecimal.ZERO,
                notaDebito != null ? notaDebito : BigDecimal.ZERO, customerName,
                module, sortOrder);
    }
}
