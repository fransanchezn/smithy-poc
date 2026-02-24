package com.example.exception.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Validation error for missing required values.
 */
public final class MissingValueValidationError extends ValidationError {

    private static final String CODE = "missing_value";

    @JsonCreator
    public MissingValueValidationError(
            @JsonProperty("detail") String detail,
            @JsonProperty("ref") String ref,
            @JsonProperty("attributes") MissingValueAttributes attributes) {
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
