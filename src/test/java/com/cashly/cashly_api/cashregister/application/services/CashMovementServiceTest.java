package com.cashly.cashly_api.cashregister.application.services;

import com.cashly.cashly_api.cashregister.domain.entities.CashRegister;
import com.cashly.cashly_api.cashregister.domain.entities.CashMovement;
import com.cashly.cashly_api.cashregister.domain.repositories.CashRegisterRepository;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementDirection;
import com.cashly.cashly_api.cashregister.domain.valueobjects.CashMovementType;
import com.cashly.cashly_api.cashregister.domain.valueobjects.RegisterId;
import com.cashly.cashly_api.payment_methods.domain.entities.PaymentMethod;
import com.cashly.cashly_api.payment_methods.domain.repositories.PaymentMethodRepository;
import com.cashly.cashly_api.payment_methods.domain.valueobjects.PaymentMethodId;
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
    @Mock PaymentMethodRepository paymentMethodRepository;

    private CashMovementService service;

    @BeforeEach
    void setUp() {
        service = new CashMovementService(registerRepository, paymentMethodRepository);
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
        PaymentMethod method = PaymentMethod.reconstitute(PaymentMethodId.of(paymentMethodId),
                "PM-0001", "Efectivo", null, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        when(registerRepository.findActiveClosure()).thenReturn(Optional.of(register));
        when(paymentMethodRepository.findById(PaymentMethodId.of(paymentMethodId))).thenReturn(Optional.of(method));
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
}
