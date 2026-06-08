package com.parsun.parsun_api.warehouses.domain.entities;

public enum MovementType {
    ENTRY,              // Manual stock entry
    EXIT,               // Manual stock exit
    ADJUSTMENT,         // Inventory adjustment
    TRANSFER_IN,        // Incoming transfer
    TRANSFER_OUT,       // Outgoing transfer
    COMPONENT_EXIT,     // Component exit (composite products)
    RETURN_ENTRY,       // Stock entry from returns
    CANCEL_ENTRY,       // Stock entry from order/invoice cancellation
    SALE_EXIT           // Stock exit from sales/invoices
}
