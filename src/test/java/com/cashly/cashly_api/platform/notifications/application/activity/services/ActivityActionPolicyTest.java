package com.cashly.cashly_api.platform.notifications.application.activity.services;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityActionPolicyTest {

    @Test
    void every_permission_action_is_classified() {
        for (PermissionAction action : PermissionAction.values()) {
            assertThat(ActivityActionPolicy.isClassified(action.name()))
                    .as(action.name()).isTrue();
        }
    }

    @Test
    void read_only_actions_never_generate_activity() {
        for (String action : ActivityActionPolicy.readOnlyActions()) {
            assertThat(ActivityActionPolicy.isMutating(action))
                    .as(action).isFalse();
        }
    }

    @Test
    void payment_actions_share_message_keys() {
        assertThat(ActivityActionPolicy.messageKey("PAY_ALL"))
                .isEqualTo(ActivityActionPolicy.messageKey("PAY_DIRECT"));
    }
}
