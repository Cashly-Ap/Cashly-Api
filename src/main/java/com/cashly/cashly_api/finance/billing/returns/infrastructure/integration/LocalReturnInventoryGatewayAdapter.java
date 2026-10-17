package com.cashly.cashly_api.finance.billing.returns.infrastructure.integration;

import com.cashly.cashly_api.finance.billing.returns.application.ports.out.ReturnInventoryGateway;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.ReturnInventoryApi;
import com.cashly.cashly_api.warehouses.stock.application.ports.in.ReturnStockCommand;
import org.springframework.stereotype.Component;

@Component
public class LocalReturnInventoryGatewayAdapter implements ReturnInventoryGateway {
    private final ReturnInventoryApi inventory;

    public LocalReturnInventoryGatewayAdapter(ReturnInventoryApi inventory) {
        this.inventory = inventory;
    }

    @Override
    public void restoreStock(Command command) {
        inventory.restoreStock(toWarehouseCommand(command));
    }

    @Override
    public void reverseStock(Command command) {
        inventory.reverseStock(toWarehouseCommand(command));
    }

    private ReturnStockCommand toWarehouseCommand(Command command) {
        return new ReturnStockCommand(command.returnId(), command.returnNumber(), command.userName(),
                command.allocations().stream().map(allocation -> new ReturnStockCommand.Allocation(
                        allocation.warehouseId(), allocation.productId(), allocation.quantity())).toList());
    }
}
