package com.parsun.parsun_api.auth.user.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record CommissionDeductionPercentage(BigDecimal value) {

    private static final BigDecimal MIN_VALUE = BigDecimal.ZERO;
    private static final BigDecimal MAX_VALUE = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public CommissionDeductionPercentage {
        if (value == null) {
            throw new IllegalArgumentException("Commission deduction percentage cannot be null");
        }
        value = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (value.compareTo(MIN_VALUE) < 0) {
            throw new IllegalArgumentException("Commission deduction percentage cannot be negative");
        }
        if (value.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Commission deduction percentage cannot exceed 100%");
        }
    }

    public static CommissionDeductionPercentage of(BigDecimal value) {
        return new CommissionDeductionPercentage(value);
    }

    public static CommissionDeductionPercentage defaultValue() {
        return new CommissionDeductionPercentage(new BigDecimal("1.00"));
    }

    public BigDecimal applyDeduction(BigDecimal nominalCommission, long months) {
        if (nominalCommission == null) {
            throw new IllegalArgumentException("Nominal commission cannot be null");
        }
        if (months <= 0) {
            return nominalCommission.setScale(SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal totalDeduction = value.multiply(BigDecimal.valueOf(months));
        BigDecimal result = nominalCommission.subtract(totalDeduction);
        return result.compareTo(MIN_VALUE) < 0 ? MIN_VALUE.setScale(SCALE, RoundingMode.HALF_UP) : result.setScale(SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return value.toPlainString() + "%";
    }
}
