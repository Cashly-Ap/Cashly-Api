package com.parsun.parsun_api.invoices.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.invoices.application.usecases.*;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InvoiceConfig {

    @Bean
    public CreateInvoiceUseCase createInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                       OrderRepository orderRepository,
                                                       CustomerRepository customerRepository,
                                                       UserRepository userRepository,
                                                       ProductRepository productRepository,
                                                       WarehouseStockRepository warehouseStockRepository) {
        return new CreateInvoiceUseCase(invoiceRepository, orderRepository, customerRepository,
                userRepository, productRepository, warehouseStockRepository);
    }

    @Bean
    public GetInvoiceByIdUseCase getInvoiceByIdUseCase(InvoiceRepository invoiceRepository,
                                                         StockEnrichmentService stockEnrichmentService) {
        return new GetInvoiceByIdUseCase(invoiceRepository, stockEnrichmentService);
    }

    @Bean
    public GetInvoicesUseCase getInvoicesUseCase(InvoiceRepository invoiceRepository) {
        return new GetInvoicesUseCase(invoiceRepository);
    }

    @Bean
    public CancelInvoiceUseCase cancelInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                      WarehouseStockRepository warehouseStockRepository,
                                                      StockMovementRepository stockMovementRepository) {
        return new CancelInvoiceUseCase(invoiceRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public DuplicateInvoiceUseCase duplicateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                            WarehouseStockRepository warehouseStockRepository,
                                                            StockMovementRepository stockMovementRepository) {
        return new DuplicateInvoiceUseCase(invoiceRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public UpdateInvoiceUseCase updateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                       ProductRepository productRepository,
                                                       WarehouseStockRepository warehouseStockRepository,
                                                       StockEnrichmentService stockEnrichmentService) {
        return new UpdateInvoiceUseCase(invoiceRepository, productRepository, warehouseStockRepository,
                stockEnrichmentService);
    }

    @Bean
    public ConfirmInvoiceUseCase confirmInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                        WarehouseStockRepository warehouseStockRepository,
                                                        StockMovementRepository stockMovementRepository) {
        return new ConfirmInvoiceUseCase(invoiceRepository, warehouseStockRepository, stockMovementRepository);
    }

    @Bean
    public DeleteInvoiceUseCase deleteInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                       OrderRepository orderRepository,
                                                       WarehouseStockRepository warehouseStockRepository) {
        return new DeleteInvoiceUseCase(invoiceRepository, orderRepository, warehouseStockRepository);
    }
}
