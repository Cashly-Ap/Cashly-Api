package com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.movements.services;

import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashRegister;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.entity.CashMovement;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.repository.CashRegisterRepository;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.finance.treasury.cash_register.sessions.application.ports.out.CashRegisterPaymentMethodGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashMovementServiceTest {

    @Mock CashRegisterRepository registerRepository;
    @Mock CashRegisterPaymentMethodGateway paymentMethods;

    private CashMovementService service;

    @BeforeEach
    void setUp() {
        service = new CashMovementService(registerRepository, paymentMethods);
    }

    @Test
    void should_RejectMonetaryOperation_When_NoRegisterIsOpen() {
        when(registerRepository.findActiveClosure()).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> service.record(
                CashMovementDirection.IN, CashMovementType.PAYMENT, BigDecimal.TEN,
                UUID.randomUUID(), "Pago", "PAYMENT", UUID.randomUUID(),
                UUID.randomUUID(), "Operator"));

        verify(registerRepository, never()).saveMovement(any());
    }

    @Test
    void should_RecordMovementInActiveRegister() {
        CashRegister register = CashRegister.open(RegisterId.generate(), LocalDate.now(),
                UUID.randomUUID(), "Operator", BigDecimal.ZERO);
        UUID paymentMethodId = UUID.randomUUID();
        when(registerRepository.findActiveClosure()).thenReturn(Optional.of(register));
        when(paymentMethods.findById(paymentMethodId)).thenReturn(Optional.of(
                new CashRegisterPaymentMethodGateway.PaymentMethod(paymentMethodId, "Efectivo")));
        when(registerRepository.saveMovement(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CashMovement movement = service.record(CashMovementDirection.IN, CashMovementType.PAYMENT,
                new BigDecimal("50.00"), paymentMethodId, "Pago recibido", "PAYMENT",
                UUID.randomUUID(), UUID.randomUUID(), "Operator");

        assertEquals(register.getId(), movement.getRegisterId());
        assertEquals("Efectivo", movement.getPaymentMethodName());
    }

    @Test
    void should_ProvideDescription_When_SourceOperationHasNoNotes() {
        CashRegister register = CashRegister.open(RegisterId.generate(), LocalDate.now(),
                UUID.randomUUID(), "Operator", BigDecimal.ZERO);
        when(registerRepository.findActiveClosure()).thenReturn(Optional.of(register));
        when(registerRepository.saveMovement(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CashMovement movement = service.record(CashMovementDirection.IN, CashMovementType.PAYMENT,
                BigDecimal.TEN, null, null, "PAYMENT", UUID.randomUUID(),
                UUID.randomUUID(), "Operator");

        assertEquals("PAYMENT", movement.getDescription());
    }

    @Test
    void should_RejectUnknownPaymentMethodWithoutSavingMovement() {
        CashRegister register = CashRegister.open(RegisterId.generate(), LocalDate.now(),
                UUID.randomUUID(), "Operator", BigDecimal.ZERO);
        UUID paymentMethodId = UUID.randomUUID();
        when(registerRepository.findActiveClosure()).thenReturn(Optional.of(register));
        when(paymentMethods.findById(paymentMethodId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.record(
                CashMovementDirection.IN, CashMovementType.PAYMENT, BigDecimal.TEN,
                paymentMethodId, "Pago", "PAYMENT", UUID.randomUUID(), UUID.randomUUID(), "Operator"));

        verify(registerRepository, never()).saveMovement(any());
    }
}
