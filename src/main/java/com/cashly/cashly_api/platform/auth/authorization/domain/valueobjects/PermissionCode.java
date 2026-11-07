package com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects;

import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionModule;
import com.cashly.cashly_api.platform.auth.authorization.domain.valueobjects.PermissionAction;

import java.util.Arrays;

import com.cashly.cashly_api.platform.auth.authorization.domain.exceptions.UnknownPermissionCodeException;

public record PermissionCode(PermissionModule module, PermissionAction action) {

    public String code() {
        return module.name() + "_" + action.name();
    }

    public static PermissionCode parse(String code) {
        if (code == null) {
            throw new UnknownPermissionCodeException(code);
        }
        return Arrays.stream(PermissionModule.values())
                .filter(module -> code.startsWith(module.name() + "_"))
                .findFirst()
                .map(module -> parseAction(module, code))
                .orElseThrow(() -> new UnknownPermissionCodeException(code));
    }

    private static PermissionCode parseAction(PermissionModule module,
            String code) {
        try {
            var actionName = code.substring(module.name().length() + 1);
            return new PermissionCode(module,
                    PermissionAction.valueOf(actionName));
        } catch (IllegalArgumentException exception) {
            throw new UnknownPermissionCodeException(code);
        }
    }
}
