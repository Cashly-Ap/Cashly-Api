package com.parsun.parsun_api.notifications.application.services;

import com.parsun.parsun_api.auth.user.domain.entities.User;
import com.parsun.parsun_api.auth.user.domain.repositories.UserRepository;
import com.parsun.parsun_api.auth.user.domain.valueobjects.UserId;
import com.parsun.parsun_api.notifications.domain.entities.Notification;
import com.parsun.parsun_api.notifications.domain.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        notificationService = new NotificationService(notificationRepository, userRepository, messagingTemplate);
    }

    @Test
    void shouldUseBudgetTerminologyForEveryCustomerQuoteNotification() {
        UUID customerId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        notificationService.notifyCustomerQuoteApproved(customerId, quoteId);
        notificationService.notifyCustomerQuoteRejected(customerId, quoteId, "Fuera de vigencia");
        notificationService.notifyCustomerQuoteConvertedToOrder(customerId, quoteId, "PED-001");
        notificationService.notifyCustomerQuoteConvertedToInvoice(customerId, quoteId, "FAC-001");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(4)).save(captor.capture());
        List<Notification> notifications = captor.getAllValues();
        assertTrue(notifications.stream().allMatch(this::usesBudgetTerminology));
        assertTrue(notifications.stream().allMatch(notification -> "QUOTE".equals(notification.getReferenceType())));
        assertTrue(notifications.stream().allMatch(notification -> quoteId.equals(notification.getReferenceId())));
    }

    @Test
    void shouldUseBudgetTerminologyForQuoteCreatedNotification() {
        UUID userId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        when(user.getId()).thenReturn(new UserId(userId));
        when(userRepository.findAll()).thenReturn(List.of(user));

        notificationService.notifyAdminsQuoteCreated(quoteId, "Cliente Demo");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification notification = captor.getValue();
        assertTrue(usesBudgetTerminology(notification));
        assertEquals("QUOTE", notification.getReferenceType());
        assertEquals(quoteId, notification.getReferenceId());
    }

    private boolean usesBudgetTerminology(Notification notification) {
        String visibleText = notification.getTitle() + " " + notification.getMessage();
        return visibleText.contains("Presupuesto")
                && !visibleText.toLowerCase().contains("cotizacion")
                && !visibleText.toLowerCase().contains("cotización");
    }
}
