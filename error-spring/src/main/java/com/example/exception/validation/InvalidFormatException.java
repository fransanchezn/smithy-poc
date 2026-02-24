package com.example.exception.validation;

import java.util.List;

/**
 * Exception thrown when input has invalid format.
 */
public final class InvalidFormatException extends ValidationErrorResponseException {

    public InvalidFormatException(InvalidFormatProblemDetail problemDetail) {
        super(problemDetail);
    }

    public InvalidFormatException() {
        super(new InvalidFormatProblemDetail());
    }

    public InvalidFormatException(List<InvalidFormatValidationError> errors) {
        super(new InvalidFormatProblemDetail(errors));
    }

    @Override
    public InvalidFormatProblemDetail getProblemDetail() {
        return (InvalidFormatProblemDetail) super.getProblemDetail();
    }

    public InvalidFormatException addError(String detail, String ref, String pattern) {
        getProblemDetail().addError(detail, ref, pattern);
        return this;
    }

    public InvalidFormatException addError(String detail, String ref, InvalidFormatAttributes attributes) {
        getProblemDetail().addError(detail, ref, attributes);
        return this;
    }
}
