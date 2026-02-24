package com.example.exception.validation;

/**
 * Validation error for invalid format issues.
 */
public final class InvalidFormatValidationError extends ValidationError {

    private static final String CODE = "invalid_format";

    public InvalidFormatValidationError(String detail, String ref, InvalidFormatAttributes attributes) {
        super(CODE, detail, ref, attributes);
    }

    public InvalidFormatValidationError(String detail, String ref, String pattern) {
        this(detail, ref, new InvalidFormatAttributes(pattern));
    }

    @Override
    public InvalidFormatAttributes getAttributes() {
        return (InvalidFormatAttributes) super.getAttributes();
    }
}
