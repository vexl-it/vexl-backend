package com.cleevio.vexl.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

public abstract class ApiException extends RuntimeException {

    protected abstract ErrorType getErrorType();


    /**
     * Use to obtain information about module to which exception belongs.
     */
    protected abstract Module getModule();

    /**
     * Use to obtain microservice-wide unique code representing the error.
     * Could be used for error handling by the clients.
     */
    @Override
    public String getMessage() {
        return Optional.ofNullable(getErrorType().getMessage()).orElse(getModule().getErrorMessage());
    }

    /**
     * Build exception code
     *
     * @return Exception code
     */
    public String getErrorCode() {
        return getModule().getErrorCode() + getErrorType().getCode();
    }

    /**
     * Should this exception be logged or ignored
     *
     * @return Should be logged
     */
    public boolean shouldBeLogged() {
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Module {

        OFFER("100", "Offer Module Error."),
        EXPORT("101", "Export Module Error."),
        ;

        /**
         * Error custom code
         */
        private final String errorCode;

        /**
         * Error custom message
         */
        private final String errorMessage;
    }
}
