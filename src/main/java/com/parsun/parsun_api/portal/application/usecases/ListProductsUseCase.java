package com.parsun.parsun_api.portal.application.usecases;

import com.parsun.parsun_api.categories.domain.entities.Category;
import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.portal.application.dto.response.PortalProductResponse;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListProductsUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    public ListProductsUseCase(CustomerRepository customerRepository,
                                CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
                                ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                WarehouseStockRepository warehouseStockRepository) {
        this.customerRepository = customerRepository;
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<PortalProductResponse> execute(UUID customerId, String search,
                                                        UUID categoryId, int page, int size) {
        Customer customer = customerRepository.findById(new CustomerId(customerId))
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));

        Set<UUID> allowedWarehouseIds = customerWarehouseAccessRepository
                .findByCustomerId(customerId).stream()
                .map(CustomerWarehouseAccess::getWarehouseId)
                .collect(Collectors.toSet());

        PageResponse<Product> productsPage = productRepository.findAllForOrder(
                search, categoryId, null, true, allowedWarehouseIds, page, size);

        Set<UUID> productIds = productsPage.content().stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        Set<UUID> categoryIds = productsPage.content().stream()
                .map(Product::getCategoryId)
                .collect(Collectors.toSet());
        Map<UUID, String> categoryNames = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<UUID, Integer> stockMap = warehouseStockRepository.findAllByProductIdIn(productIds).stream()
                .filter(s -> allowedWarehouseIds.contains(s.getWarehouseId()))
                .collect(Collectors.groupingBy(
                        WarehouseStock::getProductId,
                        Collectors.summingInt(s -> s.getQuantity() - s.getReserved())
                ));

        var content = productsPage.content().stream()
                .map(product -> toResponse(product, customer, categoryNames, stockMap))
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(), productsPage.totalPages(),
                productsPage.size(), productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private int calculateAvailableStock(UUID productId, Set<UUID> allowedWarehouseIds) {
        List<WarehouseStock> stocks = warehouseStockRepository.findByProductId(productId);
        return stocks.stream()
                .filter(s -> allowedWarehouseIds.contains(s.getWarehouseId()))
                .mapToInt(s -> s.getQuantity() - s.getReserved())
                .sum();
    }

    private PortalProductResponse toResponse(Product product, Customer customer,
                                              Map<UUID, String> categoryNames,
                                              Map<UUID, Integer> stockMap) {
        String categoryName = categoryNames.getOrDefault(product.getCategoryId(), "");

        BigDecimal customerPrice = resolveCustomerPrice(product, customer.getPriceTypeId());
        int availableStock = stockMap.getOrDefault(product.getId(), 0);

        return new PortalProductResponse(
                product.getId().toString(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId().toString(),
                categoryName,
                product.isActive(),
                product.getPhoto(),
                customerPrice,
                availableStock
        );
    }

    private BigDecimal resolveCustomerPrice(Product product, UUID priceTypeId) {
        if (priceTypeId == null) {
            return product.getPurchasePrice();
        }
        return product.getProductPrices().stream()
                .filter(pp -> priceTypeId.equals(pp.getPriceTypeId()))
                .map(pp -> pp.getPrice())
                .findFirst()
                .orElse(product.getPurchasePrice());
    }
}
