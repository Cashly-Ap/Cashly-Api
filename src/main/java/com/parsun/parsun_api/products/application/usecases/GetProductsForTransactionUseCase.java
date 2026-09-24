package com.parsun.parsun_api.products.application.usecases;

import com.parsun.parsun_api.categories.domain.repositories.CategoryRepository;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.entities.CustomerWarehouseAccess;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerWarehouseAccessRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.products.application.dto.response.ProductWithStockResponse;
import com.parsun.parsun_api.products.application.dto.response.ProductWithStockResponse.WarehouseStockItem;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.dto.PageResponse;
import com.parsun.parsun_api.warehouses.domain.entities.Warehouse;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetProductsForTransactionUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerWarehouseAccessRepository customerWarehouseAccessRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final WarehouseRepository warehouseRepository;

    public GetProductsForTransactionUseCase(CustomerRepository customerRepository,
                                       CustomerWarehouseAccessRepository customerWarehouseAccessRepository,
                                       ProductRepository productRepository,
                                       CategoryRepository categoryRepository,
                                       WarehouseStockRepository warehouseStockRepository,
                                       WarehouseRepository warehouseRepository) {
        this.customerRepository = customerRepository;
        this.customerWarehouseAccessRepository = customerWarehouseAccessRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductWithStockResponse> execute(UUID customerId, String search,
                                                       UUID categoryId, Boolean composite,
                                                       Boolean withStock, int page, int size) {
        return execute(List.of(customerId), search, categoryId, composite, withStock, page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductWithStockResponse> execute(List<UUID> customerIds, String search,
                                                       UUID categoryId, Boolean composite,
                                                       Boolean withStock, int page, int size) {
        if (customerIds == null || customerIds.isEmpty()) {
            throw new CustomerNotFoundException("Debe indicar al menos un cliente");
        }

        Customer customer = customerRepository.findById(new CustomerId(customerIds.get(0)))
                .orElseThrow(() -> new CustomerNotFoundException("Cliente no encontrado: " + customerIds.get(0)));

        Set<UUID> allowedWarehouseIds = customerIds.stream()
                .flatMap(customerId -> customerWarehouseAccessRepository.findByCustomerId(customerId).stream())
                .map(CustomerWarehouseAccess::getWarehouseId)
                .collect(Collectors.toSet());

        Map<UUID, Warehouse> warehouseMap = allowedWarehouseIds.stream()
                .map(id -> warehouseRepository.findById(id))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));

        PageResponse<Product> productsPage = productRepository.findAllForOrder(
                search, categoryId, composite, withStock, allowedWarehouseIds, page, size);

        List<ProductWithStockResponse> content = productsPage.content().stream()
                .map(product -> toResponse(product, customer, warehouseMap))
                .toList();

        return new PageResponse<>(content, productsPage.totalElements(), productsPage.totalPages(),
                productsPage.size(), productsPage.number(), productsPage.first(),
                productsPage.last(), productsPage.empty());
    }

    private ProductWithStockResponse toResponse(Product product, Customer customer,
                                             Map<UUID, Warehouse> warehouseMap) {
        String categoryName = categoryRepository.findById(product.getCategoryId())
                .map(c -> c.getName())
                .orElse("");

        BigDecimal customerPrice = resolveCustomerPrice(product, customer.getPriceTypeId());

        List<WarehouseStockItem> stockItems = warehouseStockRepository
                .findByProductId(product.getId()).stream()
                .filter(stock -> warehouseMap.containsKey(stock.getWarehouseId()))
                .map(stock -> {
                    Warehouse warehouse = warehouseMap.get(stock.getWarehouseId());
                    return new WarehouseStockItem(
                            stock.getWarehouseId().toString(),
                            warehouse.getCode(),
                            warehouse.getName(),
                            stock.getQuantity(),
                            stock.getReserved(),
                            stock.getAvailableQuantity()
                    );
                })
                .toList();

        int totalStock = stockItems.stream()
                .mapToInt(WarehouseStockItem::quantity)
                .sum();

        return new ProductWithStockResponse(
                product.getId().toString(),
                product.getCode(),
                product.getName(),
                product.getCategoryId().toString(),
                categoryName,
                product.isComposite(),
                product.isActive(),
                product.getPhoto(),
                customerPrice,
                totalStock,
                stockItems
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
