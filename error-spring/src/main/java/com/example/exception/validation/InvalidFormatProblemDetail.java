package com.example.exception.validation;

import java.util.List;

/**
 * Problem detail for invalid format validation errors.
 */
public final class InvalidFormatProblemDetail extends ValidationProblemDetail {

    public InvalidFormatProblemDetail() {
        super();
    }

    public InvalidFormatProblemDetail(List<InvalidFormatValidationError> errors) {
        super(errors);
    }

    public InvalidFormatProblemDetail addError(String detail, String ref, String pattern) {
        addError(new InvalidFormatValidationError(detail, ref, pattern));
        return this;
    }

    public InvalidFormatProblemDetail addError(String detail, String ref, InvalidFormatAttributes attributes) {
        addError(new InvalidFormatValidationError(detail, ref, attributes));
        return this;
    }
}
