package com.cashly.cashly_api.returns.application.get;

import com.cashly.cashly_api.returns.application.dto.response.ReturnResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetReturnByIdUseCase Unit Tests")
class GetReturnByIdUseCaseTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private ReturnResponseAssembler returnMapper;

    private GetReturnByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetReturnByIdUseCase(returnRepository, returnMapper);
    }

    @Test
    void should_ReturnReturnResponse_When_ReturnExists() {
        UUID id = UUID.randomUUID();
        Return return_ = buildReturn(id);
        ReturnResponse expectedResponse = mock(ReturnResponse.class);

        when(returnRepository.findById(ReturnId.of(id))).thenReturn(Optional.of(return_));
        when(returnMapper.toResponse(return_)).thenReturn(expectedResponse);

        ReturnResponse response = useCase.execute(id);

        assertSame(expectedResponse, response);
        verify(returnRepository).findById(ReturnId.of(id));
        verify(returnMapper).toResponse(return_);
    }

    @Test
    void should_ThrowReturnNotFoundException_When_ReturnDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(returnRepository.findById(ReturnId.of(id))).thenReturn(Optional.empty());

        GetReturnByIdUseCase.ReturnNotFoundException exception = assertThrows(
                GetReturnByIdUseCase.ReturnNotFoundException.class,
                () -> useCase.execute(id)
        );

        assertEquals("Return not found with id: " + id, exception.getMessage());
        verify(returnRepository).findById(ReturnId.of(id));
        verifyNoInteractions(returnMapper);
    }

    @Test
    void should_ReturnCorrectFields_When_ReturnExists() {
        UUID id = UUID.randomUUID();
        Return return_ = buildReturn(id);
        ReturnResponse expectedResponse = new ReturnResponse(
                id, "RET-2026-0001", UUID.randomUUID(), "INV-2026-0001",
                UUID.randomUUID(), "Cliente Test", "J-12345678-9",
                "AUTOMATIC", "Producto defectuoso",
                new BigDecimal("50.00"), UUID.randomUUID(), "Test User",
                LocalDateTime.now(), LocalDateTime.now(), List.of()
        );

        when(returnRepository.findById(ReturnId.of(id))).thenReturn(Optional.of(return_));
        when(returnMapper.toResponse(return_)).thenReturn(expectedResponse);

        ReturnResponse response = useCase.execute(id);

        assertEquals(expectedResponse.id(), response.id());
        assertEquals(expectedResponse.returnNumber(), response.returnNumber());
        assertEquals(expectedResponse.returnType(), response.returnType());
        assertEquals(expectedResponse.total(), response.total());
    }

    private Return buildReturn(UUID id) {
        return Return.reconstitute(
                ReturnId.of(id),
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
