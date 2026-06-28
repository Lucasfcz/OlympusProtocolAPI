package io.github.lucasfcz.olympusprotocol.exceptions;

public class PrivacyException extends BusinessException {
    public PrivacyException(String message) {
        super("you are not allowed to see this content because it's private");
    }
}
