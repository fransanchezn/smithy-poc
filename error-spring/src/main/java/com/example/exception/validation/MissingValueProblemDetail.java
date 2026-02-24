package com.example.exception.validation;

import java.util.List;

/**
 * Problem detail for missing value validation errors.
 */
public final class MissingValueProblemDetail extends ValidationProblemDetail {

    public MissingValueProblemDetail() {
        super();
    }

    public MissingValueProblemDetail(List<MissingValueValidationError> errors) {
        super(errors);
    }

    public MissingValueProblemDetail addError(String detail, String ref, String missingField) {
        addError(new MissingValueValidationError(detail, ref, missingField));
        return this;
    }

    public MissingValueProblemDetail addError(String detail, String ref, MissingValueAttributes attributes) {
        addError(new MissingValueValidationError(detail, ref, attributes));
        return this;
    }
}
