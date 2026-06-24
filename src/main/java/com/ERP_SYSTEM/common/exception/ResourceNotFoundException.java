package com.ERP_SYSTEM.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, UUID id) {
        super("Resource " + resourceName + " not found with id " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
