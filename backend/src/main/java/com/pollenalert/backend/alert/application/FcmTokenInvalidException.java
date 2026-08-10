package com.pollenalert.backend.alert.application;

public class FcmTokenInvalidException extends RuntimeException {
    public FcmTokenInvalidException(Throwable cause) {
        super(cause);
    }
}
