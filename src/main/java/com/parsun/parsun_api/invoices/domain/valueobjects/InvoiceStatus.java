package com.parsun.parsun_api.invoices.domain.valueobjects;

public enum InvoiceStatus {
    DRAFT,
    PAID,
    PENDING_PAYMENT,
    PARTIALLY_PAID,
    FULLY_RETURNED,
    CANCELLED;

    public String label() {
        return switch (this) {
            case DRAFT -> "Borrador";
            case PAID -> "Pagada";
            case PENDING_PAYMENT -> "Pendiente de Pago";
            case PARTIALLY_PAID -> "Parcialmente Pagada";
            case FULLY_RETURNED -> "Totalmente Devuelta";
            case CANCELLED -> "Cancelada";
        };
    }
}
