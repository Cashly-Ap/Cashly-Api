package com.parsun.parsun_api.suppliers.application.usecases;

import com.parsun.parsun_api.suppliers.application.dto.response.SupplierListResponse;
import com.parsun.parsun_api.suppliers.application.mappers.SupplierMapper;
import com.parsun.parsun_api.suppliers.domain.entities.Supplier;
import com.parsun.parsun_api.suppliers.domain.repositories.SupplierRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public ListSuppliersUseCase(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierListResponse> execute(String search, String type, Boolean active,
                                                       String state, int page, int size,
                                                       String orderBy, String orderDir) {
        PageResponse<Supplier> supplierPage = supplierRepository.findAllWithFilters(
            search, type, active, state, page, size, orderBy, orderDir);

        List<SupplierListResponse> content = supplierPage.content().stream()
            .map(supplierMapper::toListResponse)
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
