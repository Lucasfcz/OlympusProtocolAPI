package io.github.lucasfcz.olympusprotocol.exceptions;

import java.util.UUID;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(resource + " not found with id: " + id);
    }
}
