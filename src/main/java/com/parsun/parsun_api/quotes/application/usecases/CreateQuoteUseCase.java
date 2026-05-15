package com.parsun.parsun_api.quotes.application.usecases;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.quotes.application.dto.request.CreateQuoteRequest;
import com.parsun.parsun_api.quotes.application.dto.response.QuoteDetailResponse;
import com.parsun.parsun_api.quotes.application.mappers.QuoteMapper;
import com.parsun.parsun_api.quotes.domain.entities.Quote;
import com.parsun.parsun_api.quotes.domain.entities.QuoteAllocation;
import com.parsun.parsun_api.quotes.domain.entities.QuoteLine;
import com.parsun.parsun_api.quotes.domain.repositories.QuoteRepository;
import com.parsun.parsun_api.quotes.domain.valueobjects.QuoteNumber;
import com.parsun.parsun_api.warehouses.domain.repositories.WarehouseRepository;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CreateQuoteUseCase {

    private final QuoteRepository quoteRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public CreateQuoteUseCase(QuoteRepository quoteRepository,
                               CustomerRepository customerRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository,
                               WarehouseRepository warehouseRepository) {
        this.quoteRepository = quoteRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional
    public QuoteDetailResponse execute(CreateQuoteRequest request, UUID authenticatedUserId) {
        Customer customer = customerRepository.findById(new CustomerId(request.customerId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + request.customerId()));

        User user = userRepository.findById(new UserId(authenticatedUserId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + authenticatedUserId));

        List<QuoteLine> lines = request.lines().stream()
                .map(lineRequest -> {
                    Product product = productRepository.findById(lineRequest.productId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineRequest.productId()));

                    QuoteLine line = new QuoteLine(
                            product.getId(),
                            product.getName(),
                            product.getCode(),
                            lineRequest.quantity(),
                            lineRequest.unitPrice()
                    );

                    lineRequest.warehouseAllocations().forEach(allocationRequest -> {
                        String warehouseName = warehouseRepository.findById(allocationRequest.warehouseId())
                                .map(w -> w.getName())
                                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + allocationRequest.warehouseId()));

                        QuoteAllocation allocation = new QuoteAllocation(
                                product.getId(),
                                allocationRequest.warehouseId(),
                                warehouseName,
                                allocationRequest.allocatedQuantity()
                        );
                        line.addAllocation(allocation);
                    });

                    return line;
                })
                .toList();

        int year = LocalDate.now().getYear();
        int sequence = quoteRepository.getNextSequenceForYear(year);
        QuoteNumber quoteNumber = QuoteNumber.of(year, sequence);

        Quote quote = Quote.create(
                quoteNumber,
                customer.getId().getValue(),
                customer.getLegalName().getValue(),
                customer.getRif().getValue(),
                user.getId().getValue(),
                user.getUserName().getValue(),
                request.expiryDate(),
                request.notes(),
                lines
        );

        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toDetailResponse(saved);
    }
}
