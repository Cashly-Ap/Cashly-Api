package com.cashly.cashly_api.quotes.application.usecases;

import com.cashly.cashly_api.customers.domain.entities.CustomerWarehouseAccess;
import com.cashly.cashly_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.quotes.application.dto.response.QuoteWarehouseResponse;
import com.cashly.cashly_api.warehouses.domain.entities.Warehouse;
import com.cashly.cashly_api.warehouses.domain.repositories.WarehouseRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GetWarehousesForQuoteUseCase {

    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    private final WarehouseRepository warehouseRepository;

    public GetWarehousesForQuoteUseCase(CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
                                         WarehouseRepository warehouseRepository) {
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<QuoteWarehouseResponse> execute(UUID customerId) {
        return customerWarehouseAccessRepository.findByCustomerId(customerId).stream()
                .map(CustomerWarehouseAccess::getWarehouseId)
                .map(warehouseRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::toResponse)
                .toList();
    }

    private QuoteWarehouseResponse toResponse(Warehouse warehouse) {
        return new QuoteWarehouseResponse(
                warehouse.getId().toString(),
                warehouse.getName(),
                warehouse.getAddress()
        );
    }
}
