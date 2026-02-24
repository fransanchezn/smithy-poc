package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;

import java.util.List;

/**
 * Exception for validation errors.
 * Sealed to only permit specific validation exception implementations.
 * Detail is not supported at root level - each ValidationError has its own detail.
 */
public abstract sealed class ValidationErrorResponseException extends ApiErrorResponseException
        permits InvalidFormatException, MissingValueException {

    protected ValidationErrorResponseException(ValidationProblemDetail problemDetail) {
        super(problemDetail);
    }

    public ValidationProblemDetail getProblemDetail() {
        return (ValidationProblemDetail) getBody();
    }

    public ValidationErrorResponseException addError(ValidationError error) {
        getProblemDetail().addError(error);
        return this;
    }

    public List<ValidationError> getErrors() {
        return getProblemDetail().getErrors();
    }
}
