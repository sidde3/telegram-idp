package org.demo;

public class AuthenticationException extends Exception {
    private final String errorType;
    private final String userFriendlyMessage;

    public AuthenticationException(String logMessage, String errorType, String userFriendlyMessage) {
        super(logMessage);
        this.errorType = errorType;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getUserFriendlyMessage() {
        return userFriendlyMessage;
    }
}
