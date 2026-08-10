package com.pollenalert.backend.alert.application;

public class TransientFcmException extends RuntimeException {
    public TransientFcmException(Throwable cause) {
        super(cause);
    }
}
