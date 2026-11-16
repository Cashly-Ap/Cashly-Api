package com.cashly.cashly_api.platform.auth.user.domain.valueobjects;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserProfileTest {
    @Test
    void normalizes_unicode_names_and_one_phone() {
        var profile = new UserProfile("  José María  ", " García ", null,
                "+34 (612) 345-678");
        assertThat(profile.firstName()).isEqualTo("José María");
        assertThat(profile.lastName()).isEqualTo("García");
        assertThat(profile.phone()).isEqualTo("+34612345678");
    }

    @Test
    void accepts_absent_phone() {
        assertThat(new UserProfile("Name", "Surname", null, null).phone())
                .isNull();
        assertThat(new UserProfile("Name", "Surname", null, " ").phone())
                .isNull();
    }

    @Test
    void rejects_invalid_names_address_and_phone() {
        assertThatThrownBy(() -> new UserProfile(" ", "Name", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                () -> new UserProfile("x".repeat(101), "Name", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UserProfile.Address("x".repeat(201), null,
                null, null, null)).isInstanceOf(IllegalArgumentException.class);
        for (String phone : new String[]{"abc1234567", "123456",
                "1234567890123456", "612345678,612345679"}) {
            assertThatThrownBy(
                    () -> new UserProfile("Name", "Surname", null, phone))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
