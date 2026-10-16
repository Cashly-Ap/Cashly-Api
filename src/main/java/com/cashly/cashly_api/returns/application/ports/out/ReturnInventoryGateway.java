package com.cashly.cashly_api.returns.application.ports.out;

import com.cashly.cashly_api.returns.domain.Return;
import java.util.List;
import java.util.UUID;

public interface ReturnInventoryGateway {
    void restoreStock(Command command);
    void reverseStock(Command command);

    record Command(UUID returnId, String returnNumber, String userName, List<Allocation> allocations) {
        public Command {
            allocations = List.copyOf(allocations);
        }

        public static Command from(Return returned, String userName) {
            List<Allocation> allocations = returned.getLines().stream()
                    .flatMap(line -> line.getAllocations().stream()
                            .map(allocation -> new Allocation(allocation.getWarehouseId(), line.getProductId(),
                                    allocation.getAllocated().intValue())))
                    .filter(allocation -> allocation.quantity() > 0).toList();
            return new Command(returned.getId().value(), returned.getReturnNumber().value(), userName, allocations);
        }
    }

    record Allocation(UUID warehouseId, UUID productId, int quantity) {}
}
