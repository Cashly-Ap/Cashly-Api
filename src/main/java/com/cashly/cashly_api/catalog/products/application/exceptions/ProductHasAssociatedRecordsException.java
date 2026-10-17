package com.cashly.cashly_api.catalog.products.application.exceptions;

import java.util.UUID;

public class ProductHasAssociatedRecordsException extends RuntimeException {
    public ProductHasAssociatedRecordsException(UUID id) {
        super("No se puede eliminar el producto " + id + " porque tiene movimientos o documentos asociados (pedidos, facturas, notas, stock, etc.)");
    }
}
