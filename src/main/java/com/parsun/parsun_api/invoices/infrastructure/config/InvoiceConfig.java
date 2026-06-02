package com.parsun.parsun_api.invoices.infrastructure.config;

import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.credit_notes.domain.repositories.CreditNoteRepository;
import com.parsun.parsun_api.debit_notes.domain.repositories.DebitNoteRepository;
import com.parsun.parsun_api.invoices.application.usecases.*;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.infrastructure.services.InvoiceStatusServiceImpl;
import com.parsun.parsun_api.payments.domain.repositories.PaymentAllocationRepository;
import com.parsun.parsun_api.orders.domain.repositories.OrderRepository;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.payments.infrastructure.persistence.repositories.PaymentAllocationJpaRepository;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.shared.application.StockEnrichmentService;
import com.parsun.parsun_api.shared.services.CreditGuard;
import com.parsun.parsun_api.warehouses.domain.repositories.StockMovementRepository;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
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
                                                       WarehouseStockRepository warehouseStockRepository,
                                                       CreditGuard creditGuard) {
        return new CreateInvoiceUseCase(invoiceRepository, orderRepository, customerRepository,
                userRepository, productRepository, warehouseStockRepository, creditGuard);
    }

    @Bean
    public GetInvoiceByIdUseCase getInvoiceByIdUseCase(InvoiceRepository invoiceRepository,
                                                           StockEnrichmentService stockEnrichmentService,
                                                           CreditNoteRepository creditNoteRepository,
                                                           DebitNoteRepository debitNoteRepository,
                                                           ReturnRepository returnRepository,
                                                           ProductRepository productRepository,
                                                           WarehouseRepository warehouseRepository) {
        return new GetInvoiceByIdUseCase(invoiceRepository, stockEnrichmentService,
                creditNoteRepository, debitNoteRepository,
                returnRepository, productRepository, warehouseRepository);
    }

    @Bean
    public GetInvoicesUseCase getInvoicesUseCase(InvoiceRepository invoiceRepository,
                                                  PaymentAllocationJpaRepository paymentAllocationJpaRepository,
                                                  DebitNoteRepository debitNoteRepository,
                                                  CreditNoteRepository creditNoteRepository) {
        return new GetInvoicesUseCase(invoiceRepository, paymentAllocationJpaRepository, debitNoteRepository, creditNoteRepository);
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
                                                             ProductRepository productRepository) {
        return new DuplicateInvoiceUseCase(invoiceRepository, warehouseStockRepository, productRepository);
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
                                                         StockMovementRepository stockMovementRepository,
                                                         CreditGuard creditGuard) {
        return new ConfirmInvoiceUseCase(invoiceRepository, warehouseStockRepository, stockMovementRepository, creditGuard);
    }

    @Bean
    public DeleteInvoiceUseCase deleteInvoiceUseCase(InvoiceRepository invoiceRepository,
                                                       OrderRepository orderRepository,
                                                       WarehouseStockRepository warehouseStockRepository) {
        return new DeleteInvoiceUseCase(invoiceRepository, orderRepository, warehouseStockRepository);
    }

    @Bean
    public InvoiceStatusServiceImpl invoiceStatusService(InvoiceRepository invoiceRepository,
                                                          PaymentAllocationRepository paymentAllocationRepository,
                                                          DebitNoteRepository debitNoteRepository,
                                                          CreditNoteRepository creditNoteRepository) {
        return new InvoiceStatusServiceImpl(invoiceRepository, paymentAllocationRepository,
                debitNoteRepository, creditNoteRepository);
    }
}
