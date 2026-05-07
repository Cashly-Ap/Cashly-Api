package com.parsun.parsun_api.orders.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.orders.application.dto.request.CreateOrderRequest;
import com.parsun.parsun_api.orders.application.dto.response.OrderDetailResponse;
import com.parsun.parsun_api.orders.application.mappers.OrderMapper;
import com.parsun.parsun_api.orders.domain.entities.Order;
import com.parsun.parsun_api.orders.domain.entities.OrderLine;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.orders.domain.valueobjects.OrderNumber;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.MovementType;
import com.parsun.parsun_api.warehouses.domain.entities.StockMovement;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;

    public CreateOrderUseCase(OrderRepository orderRepository,
                               CustomerRepository customerRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               WarehouseStockRepository warehouseStockRepository,
                               StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public OrderDetailResponse execute(CreateOrderRequest request, UUID authenticatedUserId) {
        Customer customer = customerRepository.findById(new CustomerId(request.customerId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        List<OrderLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    applyStockAllocations(lineRequest, product.getId());

                    return OrderLine.create(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
                            lineRequest.quantity(),
                            lineRequest.unitPrice()
                    );
                })
                .toList();

        int year = LocalDate.now().getYear();
        int sequence = orderRepository.getNextSequenceForYear(year);
        OrderNumber orderNumber = OrderNumber.of(year, sequence);

        Order order = Order.create(
                orderNumber,
                customer.getId().getValue(),
                customer.getLegalName().getValue(),
                customer.getRif().getValue(),
                user.getId().getValue(),
                user.getUserName().getValue(),
                request.notes(),
                lines
        );

        Order saved = orderRepository.save(order);
        return OrderMapper.toDetailResponse(saved);
    }

    private void applyStockAllocations(CreateOrderRequest.OrderLineRequest lineRequest, UUID productId) {
        for (CreateOrderRequest.WarehouseAllocationRequest allocation : lineRequest.warehouseAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.warehouseId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.warehouseId() + " product=" + productId));

            int previousStock = stock.getQuantity();
            stock.removeStock(allocation.allocated());
            int newStock = stock.getQuantity();

            warehouseStockRepository.save(stock);

            StockMovement movement = StockMovement.create(
                    allocation.warehouseId(),
                    productId,
                    MovementType.EXIT,
                    allocation.allocated(),
                    previousStock,
                    newStock,
                    null,
                    null,
                    "Pedido"
            );
            stockMovementRepository.save(movement);
        }
    }
}
