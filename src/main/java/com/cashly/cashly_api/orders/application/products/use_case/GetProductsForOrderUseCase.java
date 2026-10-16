package com.cashly.cashly_api.orders.application.products.use_case;

import com.cashly.cashly_api.orders.application.ports.out.OrderCustomerGateway;
import com.cashly.cashly_api.orders.application.ports.out.OrderProductCatalogGateway;
import com.cashly.cashly_api.orders.application.dto.response.OrderProductResponse;
import com.cashly.cashly_api.orders.application.dto.response.OrderProductResponse.WarehouseStockItem;
import com.cashly.cashly_api.shared.dto.PageResponse;
import java.util.UUID;

public class GetProductsForOrderUseCase {

    private final OrderCustomerGateway customers;
    private final OrderProductCatalogGateway products;

    public GetProductsForOrderUseCase(OrderCustomerGateway customers, OrderProductCatalogGateway products) {
        this.customers = customers;
        this.products = products;
    }

    public PageResponse<OrderProductResponse> execute(UUID customerId, String search,
                                                       UUID categoryId, Boolean composite,
                                                       Boolean withStock, int page, int size) {
        customers.findById(customerId).orElseThrow(() ->
                new IllegalArgumentException("Customer not found: " + customerId));
        PageResponse<OrderProductCatalogGateway.ProductEntry> result = products.findForCustomer(customerId, search,
                categoryId, composite, withStock, page, size);
        var content = result.content().stream().map(product -> new OrderProductResponse(product.id().toString(),
                product.code(), product.name(), product.categoryId().toString(), product.categoryName(),
                product.composite(), product.active(), product.photo(), product.customerPrice(), product.totalStock(),
                product.stock().stream().map(stock -> new WarehouseStockItem(stock.warehouseId().toString(),
                        stock.warehouseCode(), stock.warehouseName(), stock.quantity(), stock.reserved(),
                        stock.available())).toList())).toList();
        return new PageResponse<>(content, result.totalElements(), result.totalPages(), result.size(), result.number(),
                result.first(), result.last(), result.empty());
    }
}
