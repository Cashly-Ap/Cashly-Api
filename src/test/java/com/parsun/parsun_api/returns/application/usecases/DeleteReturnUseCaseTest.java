package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnId;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import com.parsun.parsun_api.invoices.domain.valueobjects.InvoiceId;
import com.parsun.parsun_api.returns.domain.entities.ReturnLine;
import com.parsun.parsun_api.returns.domain.entities.ReturnNumber;
import com.parsun.parsun_api.returns.domain.entities.ReturnType;
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
@DisplayName("DeleteReturnUseCase Unit Tests")
class DeleteReturnUseCaseTest {

    @Mock
    private ReturnRepository returnRepository;

    private DeleteReturnUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteReturnUseCase(returnRepository);
    }

    @Test
    void should_DeleteReturn_When_ReturnExists() {
        UUID id = UUID.randomUUID();
        Return return_ = buildReturn(id);

        when(returnRepository.findById(ReturnId.of(id))).thenReturn(Optional.of(return_));

        useCase.execute(id);

        verify(returnRepository).findById(ReturnId.of(id));
        verify(returnRepository).delete(ReturnId.of(id));
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
        verify(returnRepository, never()).delete(any());
    }

    @Test
    void should_DeleteCorrectReturn_When_MultipleReturnsExist() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Return return1 = buildReturn(id1);

        when(returnRepository.findById(ReturnId.of(id1))).thenReturn(Optional.of(return1));

        useCase.execute(id1);

        verify(returnRepository).delete(ReturnId.of(id1));
        verify(returnRepository, never()).delete(ReturnId.of(id2));
    }

    private Return buildReturn(UUID id) {
        return Return.reconstitute(
                ReturnId.of(id),
                ReturnNumber.of(2026, 1),
                InvoiceId.of(UUID.randomUUID()),
                UUID.randomUUID(),
                ReturnType.AUTOMATIC,
                "Producto defectuoso",
                new BigDecimal("50.00"),
                List.of(ReturnLine.create(
                        UUID.randomUUID(), UUID.randomUUID(),
                        BigDecimal.ONE, new BigDecimal("50.00"), new BigDecimal("50.00"),
                        List.of()
                )),
                UUID.randomUUID(),
                "Test User",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
