package com.cashly.cashly_api.returns.application.list;

import com.cashly.cashly_api.returns.application.dto.response.ReturnListResponse;
import com.cashly.cashly_api.returns.application.services.ReturnResponseAssembler;
import com.cashly.cashly_api.returns.domain.Return;
import com.cashly.cashly_api.returns.domain.ReturnId;
import com.cashly.cashly_api.returns.domain.ReturnLine;
import com.cashly.cashly_api.returns.domain.ReturnNumber;
import com.cashly.cashly_api.returns.domain.ReturnType;
import com.cashly.cashly_api.returns.domain.ReturnRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllReturnsUseCase Unit Tests")
class GetAllReturnsUseCaseTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnResponseAssembler returnMapper;

    private GetAllReturnsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAllReturnsUseCase(returnRepository, returnMapper);
    }

    @Test
    void should_ReturnAllReturns_When_NoFilters() {
        Return return_ = buildReturn();
        ReturnListResponse listResponse = mock(ReturnListResponse.class);

        when(returnRepository.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(return_));
        when(returnMapper.toListResponse(return_)).thenReturn(listResponse);

        List<ReturnListResponse> result = useCase.execute(null, null, null, null);

        assertEquals(1, result.size());
        assertSame(listResponse, result.get(0));
        verify(returnRepository).findAllWithFilters(null, null, null, null);
        verify(returnMapper).toListResponse(return_);
    }

    @Test
    void should_ReturnFilteredReturns_When_DateRangeProvided() {
        LocalDate dateFrom = LocalDate.of(2026, 1, 1);
        LocalDate dateTo = LocalDate.of(2026, 12, 31);

        when(returnRepository.findAllWithFilters(dateFrom, dateTo, null, null))
                .thenReturn(List.of());

        List<ReturnListResponse> result = useCase.execute(dateFrom, dateTo, null, null);

        assertTrue(result.isEmpty());
        verify(returnRepository).findAllWithFilters(dateFrom, dateTo, null, null);
    }

    @Test
    void should_ReturnFilteredReturns_When_CustomerIdProvided() {
        UUID customerId = UUID.randomUUID();

        when(returnRepository.findAllWithFilters(null, null, customerId, null))
                .thenReturn(List.of());

        List<ReturnListResponse> result = useCase.execute(null, null, customerId, null);

        assertTrue(result.isEmpty());
        verify(returnRepository).findAllWithFilters(null, null, customerId, null);
    }

    @Test
    void should_ReturnFilteredReturns_When_InvoiceIdProvided() {
        UUID invoiceId = UUID.randomUUID();

        when(returnRepository.findAllWithFilters(null, null, null, invoiceId))
                .thenReturn(List.of());

        List<ReturnListResponse> result = useCase.execute(null, null, null, invoiceId);

        assertTrue(result.isEmpty());
        verify(returnRepository).findAllWithFilters(null, null, null, invoiceId);
    }

    @Test
    void should_ReturnEmptyList_When_NoReturnsMatchFilters() {
        when(returnRepository.findAllWithFilters(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<ReturnListResponse> result = useCase.execute(
                LocalDate.now().minusDays(30), LocalDate.now(), UUID.randomUUID(), UUID.randomUUID()
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void should_MapEachReturn_When_MultipleReturns() {
        Return return1 = buildReturn();
        Return return2 = buildReturn();
        ReturnListResponse resp1 = new ReturnListResponse(
                UUID.randomUUID(), "RET-2026-0001", UUID.randomUUID(), "INV-001",
                UUID.randomUUID(), "Client A", "AUTOMATIC", "Reason A",
                BigDecimal.TEN, 1, LocalDateTime.now()
        );
        ReturnListResponse resp2 = new ReturnListResponse(
                UUID.randomUUID(), "RET-2026-0002", UUID.randomUUID(), "INV-002",
                UUID.randomUUID(), "Client B", "MANUAL", "Reason B",
                BigDecimal.valueOf(20), 2, LocalDateTime.now()
        );

        when(returnRepository.findAllWithFilters(null, null, null, null))
                .thenReturn(List.of(return1, return2));
        when(returnMapper.toListResponse(return1)).thenReturn(resp1);
        when(returnMapper.toListResponse(return2)).thenReturn(resp2);

        List<ReturnListResponse> result = useCase.execute(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals("Client A", result.get(0).customerName());
        assertEquals("Client B", result.get(1).customerName());
    }

    private Return buildReturn() {
        return Return.reconstitute(
                ReturnId.generate(),
                ReturnNumber.of(2026, 1),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ReturnType.AUTOMATIC,
                "Producto defectuoso",
                new BigDecimal("50.00"),
                List.of(ReturnLine.create(
                        UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                        List.of()
                )),
                List.of(),
                UUID.randomUUID(),
                "Test User",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
