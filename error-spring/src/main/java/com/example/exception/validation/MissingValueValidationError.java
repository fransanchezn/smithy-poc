package com.example.exception.validation;

/**
 * Validation error for missing required values.
 */
public final class MissingValueValidationError extends ValidationError {

    private static final String CODE = "missing_value";

    public MissingValueValidationError(String detail, String ref, MissingValueAttributes attributes) {
        super(CODE, detail, ref, attributes);
    }

    public MissingValueValidationError(String detail, String ref, String missingField) {
        this(detail, ref, new MissingValueAttributes(missingField));
    }

    @Override
    public MissingValueAttributes getAttributes() {
        return (MissingValueAttributes) super.getAttributes();
    }
}
