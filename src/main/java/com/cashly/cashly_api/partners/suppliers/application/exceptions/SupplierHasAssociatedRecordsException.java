package com.cashly.cashly_api.partners.suppliers.application.exceptions;

public class SupplierHasAssociatedRecordsException extends RuntimeException {
    public SupplierHasAssociatedRecordsException(String id) {
        super("No se puede eliminar el proveedor " + id + " porque tiene compras asociadas");
    }
}
