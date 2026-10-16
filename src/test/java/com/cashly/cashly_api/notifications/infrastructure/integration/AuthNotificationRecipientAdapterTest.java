package com.cashly.cashly_api.notifications.infrastructure.integration;

import com.cashly.cashly_api.auth.user.application.ports.in.NotificationRecipientsQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthNotificationRecipientAdapterTest {
    @Test
    void shouldPreserveRecipientsFromPublicAuthContract() {
        NotificationRecipientsQuery query = mock(NotificationRecipientsQuery.class);
        List<UUID> recipients = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(query.findAllUserIds()).thenReturn(recipients);

        assertEquals(recipients, new AuthNotificationRecipientAdapter(query).findAllUserIds());
        verify(query).findAllUserIds();
    }

    @Test
    void shouldPreserveEmptyRecipientList() {
        NotificationRecipientsQuery query = mock(NotificationRecipientsQuery.class);
        when(query.findAllUserIds()).thenReturn(List.of());

        assertEquals(List.of(), new AuthNotificationRecipientAdapter(query).findAllUserIds());
    }
}
