package com.example.exception.domain;

import com.example.exception.ApiErrorResponseException;
import com.example.exception.ErrorAttributes;

/**
 * Exception for domain-specific business errors.
 * Sealed to only permit specific domain exception implementations.
 */
public abstract sealed class DomainErrorResponseException extends ApiErrorResponseException
        permits TransferLimitExceededException, AccountSuspendedException {

    protected DomainErrorResponseException(DomainProblemDetail problemDetail) {
        super(problemDetail);
    }

    protected DomainErrorResponseException(DomainProblemDetail problemDetail, Throwable cause) {
        super(problemDetail, cause);
    }

    public DomainProblemDetail getProblemDetail() {
        return (DomainProblemDetail) getBody();
    }

    public String getCode() {
        return getProblemDetail().getCode();
    }

    public ErrorAttributes getAttributes() {
        return getProblemDetail().getAttributes();
    }
}
