package com.cashly.cashly_api.quotes.application.create.use_case;

import com.cashly.cashly_api.quotes.application.ports.out.ActorIdentityGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteCustomerGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteProductCatalogGateway;
import com.cashly.cashly_api.quotes.application.ports.out.QuoteWarehouseGateway;
import com.cashly.cashly_api.quotes.application.create.request.CreateQuoteRequest;
import com.cashly.cashly_api.quotes.domain.entity.Quote;
import com.cashly.cashly_api.quotes.domain.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateQuoteUseCaseTest {
    @Mock QuoteRepository quotes;
    @Mock QuoteCustomerGateway customers;
    @Mock ActorIdentityGateway actors;
    @Mock QuoteProductCatalogGateway products;
    @Mock QuoteWarehouseGateway warehouses;

    private CreateQuoteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateQuoteUseCase(quotes, customers, actors, products, warehouses);
    }

    @Test
    void shouldCreateQuoteUsingPublicContractsAndSnapshotCategory() {
        UUID customerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        AtomicReference<Quote> saved = new AtomicReference<>();
        when(customers.findById(customerId)).thenReturn(Optional.of(
                new QuoteCustomerGateway.Customer(customerId, "Acme", "RIF", null)));
        when(actors.findActor(actorId)).thenReturn(Optional.of(new ActorIdentityGateway.Actor(actorId, "Alex")));
        when(products.findProduct(productId)).thenReturn(Optional.of(
                new QuoteProductCatalogGateway.ProductReference(productId, "P-1", "Product", categoryId)));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(
                new QuoteWarehouseGateway.Warehouse(warehouseId, "Main", "Street")));
        when(quotes.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quotes.save(any(Quote.class))).thenAnswer(invocation -> {
            Quote quote = invocation.getArgument(0);
            saved.set(quote);
            return quote;
        });
        var request = new CreateQuoteRequest(customerId, null, null, List.of(
                new CreateQuoteRequest.QuoteLineRequest(productId, BigDecimal.ONE, BigDecimal.TEN, List.of(
                        new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.ONE)))));

        var response = useCase.execute(request, actorId);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.createdByName()).isEqualTo("Alex");
        assertThat(saved.get().getLines()).singleElement().extracting("categoryId").isEqualTo(categoryId);
    }

    @Test
    void shouldFallbackToAuthenticatedActorWhenDirectoryHasNoActor() {
        UUID customerId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(Optional.of(
                new QuoteCustomerGateway.Customer(customerId, "Acme", "RIF", null)));
        when(actors.findActor(actorId)).thenReturn(Optional.empty());
        when(products.findProduct(productId)).thenReturn(Optional.of(
                new QuoteProductCatalogGateway.ProductReference(productId, "P-1", "Product", null)));
        when(warehouses.findById(warehouseId)).thenReturn(Optional.of(
                new QuoteWarehouseGateway.Warehouse(warehouseId, "Main", null)));
        when(quotes.getNextSequenceForYear(anyInt())).thenReturn(1);
        when(quotes.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var request = new CreateQuoteRequest(customerId, null, null, List.of(
                new CreateQuoteRequest.QuoteLineRequest(productId, BigDecimal.ONE, BigDecimal.TEN, List.of(
                        new CreateQuoteRequest.WarehouseAllocationRequest(warehouseId, BigDecimal.ONE)))));

        var response = useCase.execute(request, actorId);

        assertThat(response.createdBy()).isEqualTo(actorId);
        assertThat(response.createdByName()).isEqualTo("Acme");
    }

    @Test
    void shouldRejectMissingCustomerBeforeLookingUpProducts() {
        UUID customerId = UUID.randomUUID();
        when(customers.findById(customerId)).thenReturn(Optional.empty());
        var request = new CreateQuoteRequest(customerId, null, null, List.of());

        assertThatThrownBy(() -> useCase.execute(request, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Customer not found");
        verifyNoInteractions(products);
        verify(quotes, never()).save(any());
    }
}
