package com.parsun.parsun_api.categories.application.exceptions;

public class DuplicateCategoryNameException extends RuntimeException {
    public DuplicateCategoryNameException(String name) {
        super("Category with name already exists: " + name);
    }
}
