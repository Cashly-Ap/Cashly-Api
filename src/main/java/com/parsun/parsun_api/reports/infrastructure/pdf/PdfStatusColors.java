package com.parsun.parsun_api.reports.infrastructure.pdf;

import com.itextpdf.kernel.colors.DeviceRgb;

public final class PdfStatusColors {

    private static final DeviceRgb INFO = new DeviceRgb(59, 130, 246);
    private static final DeviceRgb SUCCESS = new DeviceRgb(34, 197, 94);
    private static final DeviceRgb WARNING = new DeviceRgb(245, 158, 11);
    private static final DeviceRgb DANGER = new DeviceRgb(239, 68, 68);

    private PdfStatusColors() {
    }

    public static DeviceRgb forStatus(String status) {
        if (status == null) {
            return INFO;
        }

        return switch (status) {
            case "PAID", "CONFIRMED", "INVOICED", "DISPATCHED", "DELIVERED",
                 "CONVERTED", "CONVERTED_TO_ORDER", "CONVERTED_TO_INVOICE" -> SUCCESS;
            case "PENDING_PAYMENT", "CUSTOMER_DRAFT", "EXPIRED" -> WARNING;
            case "CANCELLED", "OVERDUE" -> DANGER;
            default -> INFO;
        };
    }
}
