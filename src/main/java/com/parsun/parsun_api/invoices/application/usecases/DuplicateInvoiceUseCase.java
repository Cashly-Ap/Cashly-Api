package com.parsun.parsun_api.invoices.application.usecases;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.invoices.application.dto.response.InvoiceDetailResponse;
import com.parsun.parsun_api.invoices.application.mappers.InvoiceMapper;
import com.parsun.parsun_api.invoices.domain.entities.Invoice;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLine;
import com.parsun.parsun_api.invoices.domain.entities.InvoiceLineAllocation;
import com.parsun.parsun_api.invoices.domain.exceptions.InvoiceNotFoundException;
import com.parsun.parsun_api.invoices.domain.repositories.InvoiceRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceNumber;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.warehouses.domain.entities.WarehouseStock;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseStockRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DuplicateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public DuplicateInvoiceUseCase(InvoiceRepository invoiceRepository,
                                    WarehouseStockRepository warehouseStockRepository,
                                    ProductRepository productRepository,
                                    CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.warehouseStockRepository = warehouseStockRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public InvoiceDetailResponse execute(UUID id, UUID newCustomerId) {
        Invoice original = invoiceRepository.findById(InvoiceId.of(id))
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        UUID customerId = original.getCustomerId();
        String customerName = original.getCustomerName();
        String customerRif = original.getCustomerRif();

        if (newCustomerId != null) {
            Customer customer = customerRepository.findById(new CustomerId(newCustomerId))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Customer not found: " + newCustomerId));
            customerId = customer.getId().getValue();
            customerName = customer.getLegalName().getValue();
            customerRif = customer.getRif().getValue();
        }

        int year = LocalDate.now().getYear();
        int sequence = invoiceRepository.getNextSequenceForYear(year);
        InvoiceNumber newNumber = InvoiceNumber.of(year, sequence);

        List<InvoiceLine> copiedLines = original.getLines().stream()
                .map(line -> {
                    Product product = productRepository.findById(line.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Product not found: " + line.getProductId()));

                    BigDecimal currentPrice = line.getUnitPrice();

                    List<InvoiceLineAllocation> copiedAllocations = applyStockAllocations(line);

                    return InvoiceLine.create(
                            line.getProductId(),
                            product.getName(),
                            product.getCode(),
                            line.getQuantity(),
                            currentPrice,
                            copiedAllocations
                    );
                })
                .toList();

        Invoice duplicate = Invoice.create(
                newNumber,
                customerId,
                customerName,
                customerRif,
                original.getCreatedBy(),
                original.getCreatedByName(),
                null,
                null,
                original.getPaymentType(),
                original.getNotes(),
                copiedLines
        );

        Invoice saved = invoiceRepository.save(duplicate);
        return InvoiceMapper.toDetailResponse(saved);
    }

    private List<InvoiceLineAllocation> applyStockAllocations(InvoiceLine line) {
        List<InvoiceLineAllocation> allocations = new ArrayList<>();
        for (InvoiceLineAllocation allocation : line.getAllocations()) {
            WarehouseStock stock = warehouseStockRepository
                    .findByWarehouseIdAndProductId(allocation.getWarehouseId(), line.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Stock not found for warehouse=" + allocation.getWarehouseId()
                                    + " product=" + line.getProductId()));

            stock.reserve(allocation.getAllocated().intValue());
            warehouseStockRepository.save(stock);

            allocations.add(InvoiceLineAllocation.create(allocation.getWarehouseId(), allocation.getAllocated()));
        }
        return allocations;
    }
}
