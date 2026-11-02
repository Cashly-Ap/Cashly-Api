package com.cashly.cashly_api.partners.suppliers.application.queries.use_case;

import com.cashly.cashly_api.partners.suppliers.application.ports.out.PurchaseUsageGateway;
import com.cashly.cashly_api.partners.suppliers.application.dto.response.SupplierListResponse;
import com.cashly.cashly_api.partners.suppliers.application.mappers.SupplierMapper;
import com.cashly.cashly_api.partners.suppliers.domain.entity.Supplier;
import com.cashly.cashly_api.partners.suppliers.domain.repository.SupplierRepository;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.shared.domain.valueobjects.DocumentAction;
import com.cashly.cashly_api.shared.dto.PageResponses;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseUsageGateway purchaseUsage;

    public ListSuppliersUseCase(SupplierRepository supplierRepository,
            SupplierMapper supplierMapper, PurchaseUsageGateway purchaseUsage) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
        this.purchaseUsage = purchaseUsage;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierListResponse> execute(String search,
            String type, Boolean active, String state, int page, int size,
            String orderBy, String orderDir) {
        PageResponse<Supplier> supplierPage = supplierRepository
                .findAllWithFilters(search, type, active, state, page, size,
                        orderBy, orderDir);

        Set<UUID> supplierIds = supplierPage.content().stream()
                .map(supplier -> supplier.getId().getValue())
                .collect(Collectors.toSet());
        Set<UUID> supplierIdsWithPurchases = supplierIds.isEmpty()
                ? Set.of()
                : purchaseUsage.findSupplierIdsWithPurchases(supplierIds);

        List<SupplierListResponse> content = supplierPage
                .content().stream().map(
                        s -> supplierMapper.toListResponse(s,
                                supplierIdsWithPurchases
                                        .contains(s.getId().getValue())))
                .toList();

        return new PageResponse<>(content, supplierPage.totalElements(),
                supplierPage.totalPages(), supplierPage.size(),
                supplierPage.number(), supplierPage.first(),
                supplierPage.last(), supplierPage.empty());
    }
    public PageResponse<SupplierListResponse> execute(String search, String type, Boolean active, String state,
            int page, int size, String orderBy, String orderDir, DocumentAction action) {
        if (action != DocumentAction.DELETE) return execute(search, type, active, state, page, size, orderBy, orderDir);
        var all = execute(search, type, active, state, 0, Integer.MAX_VALUE, orderBy, orderDir);
        return PageResponses.filterAndPage(all.content(), supplier -> !supplier.hasPurchases(), page, size);
    }
}
