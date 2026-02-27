package com.example.exception.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Validation error for missing required values.
 */
public final class MissingValueValidationError extends ValidationError {

  private static final String CODE = "missing_value";

  @JsonCreator
  private MissingValueValidationError(
      @JsonProperty("detail") String detail,
      @JsonProperty("ref") String ref,
      @JsonProperty("attributes") MissingValueAttributes attributes) {
    super(CODE, detail, ref, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public MissingValueAttributes getAttributes() {
    return (MissingValueAttributes) super.getAttributes();
  }

  public static final class Builder
      extends ValidationError.Builder<MissingValueAttributes, MissingValueValidationError> {

    private Builder() {
    }

    @Override
    public MissingValueValidationError build() {
      return new MissingValueValidationError(detail, ref, attributes);
    }
  }
}
