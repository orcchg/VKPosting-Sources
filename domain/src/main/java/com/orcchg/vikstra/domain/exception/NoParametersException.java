package com.orcchg.vikstra.domain.exception;

public class NoParametersException extends RuntimeException {

    public NoParametersException() {
    }

    public NoParametersException(String message) {
        super(message);
    }
}
