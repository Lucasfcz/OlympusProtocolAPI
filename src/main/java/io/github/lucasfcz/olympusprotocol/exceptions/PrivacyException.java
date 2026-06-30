package io.github.lucasfcz.olympusprotocol.exceptions;

public class PrivacyException extends BusinessException {
    public PrivacyException(String message) {
        super(message);
    }

    public PrivacyException() {
        super("You are not allowed to see this content because it's private.");
    }
}