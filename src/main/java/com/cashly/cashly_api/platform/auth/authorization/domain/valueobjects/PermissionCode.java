package com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects;

import java.util.Arrays;

public record PermissionCode(PermissionModule module, PermissionAction action) {

    public String code() {
        return module.name() + "_" + action.name();
    }

    public static PermissionCode parse(String code) {
        return Arrays.stream(PermissionModule.values())
                .filter(module -> code.startsWith(module.name() + "_"))
                .findFirst()
                .map(module -> new PermissionCode(module, PermissionAction
                        .valueOf(code.substring(module.name().length() + 1))))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown permission code: " + code));
    }

    public static PermissionCode of(PermissionModule module,
            PermissionAction action) {
        return new PermissionCode(module, action);
    }
}
