package com.cashly.cashly_api.orders.application.ports.in;

import java.util.List;
import java.util.UUID;

public interface OrderDocumentQuery {
    OrderDocumentSnapshot find(UUID id);
    List<OrderDocumentRow> findAll(OrderDocumentFilter filter);
}
