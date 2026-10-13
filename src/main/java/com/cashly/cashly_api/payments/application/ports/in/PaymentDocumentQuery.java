package com.cashly.cashly_api.payments.application.ports.in;
import java.util.UUID;
public interface PaymentDocumentQuery { PaymentDocumentSnapshot find(UUID id); }
