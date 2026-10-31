package com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects;

public enum MovementType {
    ENTRY, EXIT, ADJUSTMENT, TRANSFER_IN, TRANSFER_OUT, COMPONENT_EXIT, RETURN_ENTRY, CANCEL_ENTRY, SALE_EXIT;

    public boolean isEntry() {
        return switch (this) {
            case ENTRY, TRANSFER_IN, RETURN_ENTRY, CANCEL_ENTRY -> true;
            case EXIT, ADJUSTMENT, TRANSFER_OUT, COMPONENT_EXIT, SALE_EXIT ->
                false;
        };
    }

    public boolean isExit() {
        return switch (this) {
            case EXIT, TRANSFER_OUT, COMPONENT_EXIT, SALE_EXIT -> true;
            case ENTRY, ADJUSTMENT, TRANSFER_IN, RETURN_ENTRY, CANCEL_ENTRY ->
                false;
        };
    }
}
