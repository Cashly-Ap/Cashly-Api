package com.cashly.cashly_api.platform.audit.application.support.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AuditValueExtractor {

    private static final List<String> ID_METHODS = List.of("id", "getId",
            "value", "getValue", "entityId", "invoiceId", "orderId", "quoteId",
            "purchaseId", "paymentId", "returnId", "noteId", "creditNoteId",
            "debitNoteId", "warehouseId", "productId", "customerId",
            "supplierId", "userId", "roleId", "departmentId", "registerId",
            "movementId", "paymentMethodId", "sourceWarehouseId");

    private static final List<String> LABEL_METHODS = List.of("label",
            "reference", "number", "code", "name", "documentNumber",
            "invoiceNumber", "orderNumber", "quoteNumber", "purchaseNumber",
            "returnNumber", "creditNoteNumber", "debitNoteNumber",
            "paymentNumber", "userName", "email", "legalName", "tradeName",
            "fullName", "displayName", "description");

    private AuditValueExtractor() {
    }

    public static UUID uuid(Object value) {
        return uuid(value, 0);
    }

    public static String label(Object value) {
        return label(value, 0);
    }

    public static UUID firstUuid(Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        for (Object argument : arguments) {
            UUID value = uuid(argument);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static String firstLabel(Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        for (Object argument : arguments) {
            String value = label(argument);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public static UUID fallbackId(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static UUID uuid(Object value, int depth) {
        if (value == null || depth > 4) {
            return null;
        }
        if (value instanceof UUID id) {
            return id;
        }
        if (value instanceof CharSequence text) {
            try {
                return UUID.fromString(text.toString().trim());
            } catch (IllegalArgumentException invalid) {
                return null;
            }
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> uuid(item, depth + 1))
                    .filter(java.util.Objects::nonNull).findFirst().orElse(null);
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.get("id");
            return id == null ? null : uuid(id, depth + 1);
        }
        for (String method : ID_METHODS) {
            Object result = invoke(value, method);
            if (result != null && result != value) {
                UUID id = uuid(result, depth + 1);
                if (id != null) {
                    return id;
                }
            }
        }
        return null;
    }

    private static String label(Object value, int depth) {
        if (value == null || depth > 4) {
            return null;
        }
        if (value instanceof CharSequence text) {
            String label = text.toString().trim();
            return label.isEmpty() ? null : label;
        }
        if (value instanceof UUID) {
            return null;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> label(item, depth + 1))
                    .filter(java.util.Objects::nonNull).findFirst().orElse(null);
        }
        for (String method : LABEL_METHODS) {
            Object result = invoke(value, method);
            if (result != null && result != value) {
                String label = label(result, depth + 1);
                if (label != null && !label.isBlank()) {
                    return label;
                }
            }
        }
        return null;
    }

    private static Object invoke(Object target, String method) {
        try {
            var accessor = target.getClass().getMethod(method);
            accessor.setAccessible(true);
            return accessor.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException
                | InvocationTargetException | SecurityException unavailable) {
            return null;
        }
    }
}
