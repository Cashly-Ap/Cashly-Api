package com.parsun.parsun_api.cashregister.application.usecases;

import com.parsun.parsun_api.cashregister.domain.entities.CashRegister;
import com.parsun.parsun_api.cashregister.domain.repositories.CashRegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenRegisterUseCase Unit Tests")
class OpenRegisterUseCaseUnitTest {

    @Mock
    private CashRegisterRepository closureRepository;

    private OpenRegisterUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final String userName = "Operator Test";

    @BeforeEach
    void setUp() {
        useCase = new OpenRegisterUseCase(closureRepository);
    }

    @Test
    void should_OpenClosure_When_NoExistingClosureForToday() {
        when(closureRepository.findByClosureDate(LocalDate.now())).thenReturn(Optional.empty());
        when(closureRepository.save(any(CashRegister.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(userId, userName);

        assertNotNull(response);
        assertEquals(userId, response.openedBy());
        assertEquals(userName, response.openedByName());
        assertEquals(LocalDate.now(), response.closureDate());
        verify(closureRepository).findByClosureDate(LocalDate.now());
        verify(closureRepository).save(any(CashRegister.class));
    }

    @Test
    void should_ThrowException_When_ClosureAlreadyExistsForToday() {
        when(closureRepository.findByClosureDate(LocalDate.now()))
                .thenReturn(Optional.of(mock(CashRegister.class)));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(userId, userName)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(closureRepository, never()).save(any());
    }
}
