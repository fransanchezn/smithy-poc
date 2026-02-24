package com.example.exception.validation;

import java.util.List;

/**
 * Exception thrown when required values are missing.
 */
public final class MissingValueException extends ValidationErrorResponseException {

    public MissingValueException(MissingValueProblemDetail problemDetail) {
        super(problemDetail);
    }

    public MissingValueException() {
        super(new MissingValueProblemDetail());
    }

    public MissingValueException(List<MissingValueValidationError> errors) {
        super(new MissingValueProblemDetail(errors));
    }

    @Override
    public MissingValueProblemDetail getProblemDetail() {
        return (MissingValueProblemDetail) super.getProblemDetail();
    }

    public MissingValueException addError(String detail, String ref, String missingField) {
        getProblemDetail().addError(detail, ref, missingField);
        return this;
    }

    public MissingValueException addError(String detail, String ref, MissingValueAttributes attributes) {
        getProblemDetail().addError(detail, ref, attributes);
        return this;
    }
}
