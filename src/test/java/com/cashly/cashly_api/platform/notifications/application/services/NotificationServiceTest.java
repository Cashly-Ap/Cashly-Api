package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationRecipientGateway;
import com.cashly.cashly_api.platform.notifications.infrastructure.messaging.WebSocketNotificationDeliveryAdapter;

import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository;
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRecipientGateway recipientGateway;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository,
                recipientGateway,
                new WebSocketNotificationDeliveryAdapter(messagingTemplate,
                        org.mockito.Mockito.mock(
                                com.cashly.cashly_api.platform.auth.preferences.domain.repository.UserPreferencesRepository.class,
                                org.mockito.Mockito.CALLS_REAL_METHODS)));
    }

    @Test
    void shouldUseBudgetTerminologyForEveryCustomerQuoteNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UUID customerId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        notificationService.notifyCustomerQuoteApproved(customerId, quoteId);
        notificationService.notifyCustomerQuoteRejected(customerId, quoteId,
                "Fuera de vigencia");
        notificationService.notifyCustomerQuoteConvertedToOrder(customerId,
                quoteId, "PED-001");
        notificationService.notifyCustomerQuoteConvertedToInvoice(customerId,
                quoteId, "FAC-001");

        ArgumentCaptor<Notification> captor = ArgumentCaptor
                .forClass(Notification.class);
        verify(notificationRepository, times(4)).save(captor.capture());
        List<Notification> notifications = captor.getAllValues();
        assertTrue(
                notifications.stream().allMatch(this::usesBudgetTerminology));
        assertTrue(notifications.stream().allMatch(notification -> "QUOTE"
                .equals(notification.getReferenceType())));
        assertTrue(notifications.stream().allMatch(
                notification -> quoteId.equals(notification.getReferenceId())));
    }

    @Test
    void shouldUseBudgetTerminologyForQuoteCreatedNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UUID userId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        when(recipientGateway.findAllUserIds()).thenReturn(List.of(userId));

        notificationService.notifyAdminsQuoteCreated(quoteId, "Cliente Demo");

        ArgumentCaptor<Notification> captor = ArgumentCaptor
                .forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();
        assertTrue(usesBudgetTerminology(notification));
        assertEquals("QUOTE", notification.getReferenceType());
        assertEquals(quoteId, notification.getReferenceId());
    }

    @AfterEach
    void clearTransaction() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void shouldDeliverOnlyAfterCommit() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        notificationService.notifyCustomerInvoiceConfirmed(UUID.randomUUID(),
                UUID.randomUUID(), "INV-001");
        org.mockito.Mockito.verifyNoInteractions(messagingTemplate);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(messagingTemplate).convertAndSend(any(String.class),
                any(Notification.class));
    }

    @Test
    void shouldNotDeliverAfterRollback() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        notificationService.notifyCustomerInvoiceConfirmed(UUID.randomUUID(),
                UUID.randomUUID(), "INV-001");
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK));
        org.mockito.Mockito.verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldDeliverImmediatelyWithoutTransaction() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        notificationService.notifyCustomerInvoiceConfirmed(UUID.randomUUID(),
                UUID.randomUUID(), "INV-001");
        verify(messagingTemplate).convertAndSend(any(String.class),
                any(Notification.class));
    }

    @Test
    void shouldNotifyEveryRecipientReturnedByGateway() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(recipientGateway.findAllUserIds())
                .thenReturn(List.of(first, second));
        notificationService.notifyAdminsOrderCreated(UUID.randomUUID(),
                "Customer");
        ArgumentCaptor<Notification> captor = ArgumentCaptor
                .forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());
        assertEquals(List.of(first, second), captor.getAllValues().stream()
                .map(Notification::getUserId).toList());
    }

    @Test
    void shouldDeliverUserNotificationToExistingUserTopic() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        notificationService.notifyPaymentApproved(userId, paymentId);
        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers
                        .eq("/topic/notifications/user/" + userId),
                any(Notification.class));
    }

    @Test
    void shouldDeliverCustomerNotificationToExistingCustomerTopicAfterCommit() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UUID customerId = UUID.randomUUID();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        notificationService.notifyCustomerPaymentApproved(customerId,
                UUID.randomUUID());
        org.mockito.Mockito.verifyNoInteractions(messagingTemplate);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(messagingTemplate).convertAndSend(
                org.mockito.ArgumentMatchers
                        .eq("/topic/notifications/customer/" + customerId),
                any(Notification.class));
    }

    @Test
    void shouldNotDeliverWhenNotificationPersistenceFails() {
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new IllegalStateException("Persistence failed"));
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> notificationService.notifyCustomerPaymentApproved(
                        UUID.randomUUID(), UUID.randomUUID()));
        org.mockito.Mockito.verifyNoInteractions(messagingTemplate);
    }

    @Test
    void shouldNotFailCommittedOperationWhenWebSocketDeliveryFails() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito
                .doThrow(new org.springframework.messaging.MessagingException(
                        "WebSocket unavailable"))
                .when(messagingTemplate)
                .convertAndSend(any(String.class), any(Notification.class));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        notificationService.notifyCustomerInvoiceConfirmed(UUID.randomUUID(),
                UUID.randomUUID(), "INV-001");

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> TransactionSynchronizationManager.getSynchronizations()
                        .forEach(TransactionSynchronization::afterCommit));
        verify(notificationRepository).save(any(Notification.class));
    }

    private boolean usesBudgetTerminology(Notification notification) {
        notification = new com.cashly.cashly_api.platform.notifications.infrastructure.messaging.NotificationTranslator()
                .render(notification, java.util.Locale.forLanguageTag("es"));
        String visibleText = notification.getTitle() + " "
                + notification.getMessage();
        return visibleText.contains("Presupuesto")
                && !visibleText.toLowerCase().contains("cotizacion")
                && !visibleText.toLowerCase().contains("cotización");
    }
}
