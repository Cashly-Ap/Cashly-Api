package com.cashly.cashly_api.suppliers.application.usecases;

import com.cashly.cashly_api.purchases.domain.repositories.PurchaseRepository;
import com.cashly.cashly_api.suppliers.application.dto.response.SupplierListResponse;
import com.cashly.cashly_api.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.suppliers.domain.entities.Supplier;
import com.cashly.cashly_api.suppliers.domain.repositories.SupplierRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseRepository purchaseRepository;

    public ListSuppliersUseCase(SupplierRepository supplierRepository,
                                 SupplierMapper supplierMapper,
                                 PurchaseRepository purchaseRepository) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierListResponse> execute(String search, String type, Boolean active,
                                                       String state, int page, int size,
                                                       String orderBy, String orderDir) {
        PageResponse<Supplier> supplierPage = supplierRepository.findAllWithFilters(
            search, type, active, state, page, size, orderBy, orderDir);

        Set<UUID> supplierIdsWithPurchases = supplierPage.content().stream()
                .map(supplier -> UUID.fromString(supplier.getId().toString()))
                .filter(id -> purchaseRepository.existsBySupplierId(id))
                .collect(Collectors.toSet());

        List<SupplierListResponse> content = supplierPage.content().stream()
            .map(s -> supplierMapper.toListResponse(
                s, supplierIdsWithPurchases.contains(UUID.fromString(s.getId().toString()))))
            .toList();

        return new PageResponse<>(
            content,
            supplierPage.totalElements(),
            supplierPage.totalPages(),
            supplierPage.size(),
            supplierPage.number(),
            supplierPage.first(),
            supplierPage.last(),
            supplierPage.empty()
        );
    }
}
