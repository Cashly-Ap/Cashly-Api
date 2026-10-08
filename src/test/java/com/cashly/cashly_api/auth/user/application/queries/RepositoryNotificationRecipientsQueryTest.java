package com.cashly.cashly_api.auth.user.application.queries;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserId;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RepositoryNotificationRecipientsQueryTest {
    @Test
    void should_return_every_user_id_without_role_or_activity_filtering() {
        UserRepository repository = mock(UserRepository.class);
        User first = mock(User.class);
        User second = mock(User.class);
        UserId firstId = UserId.generate();
        UserId secondId = UserId.generate();
        when(first.getId()).thenReturn(firstId);
        when(second.getId()).thenReturn(secondId);
        when(repository.findAll()).thenReturn(List.of(first, second));
        assertEquals(List.of(firstId.getValue(), secondId.getValue()),
                new RepositoryNotificationRecipientsQuery(repository).findAllUserIds());
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
        verify(first).getId();
        verify(second).getId();
        verifyNoMoreInteractions(first, second);
    }

    @Test
    void should_return_empty_recipients_when_no_users_exist() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(new RepositoryNotificationRecipientsQuery(repository).findAllUserIds().isEmpty());
    }
}
