package com.example.exception.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Validation error for invalid format issues.
 */
public final class InvalidFormatValidationError extends ValidationError {

  private static final String CODE = "invalid_format";

  @JsonCreator
  public InvalidFormatValidationError(
      @JsonProperty("detail") String detail,
      @JsonProperty("ref") String ref,
      @JsonProperty("attributes") InvalidFormatAttributes attributes) {
    super(CODE, detail, ref, attributes);
  }

  public InvalidFormatValidationError(String detail, String ref, String pattern) {
    this(detail, ref, new InvalidFormatAttributes(pattern));
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public InvalidFormatAttributes getAttributes() {
    return (InvalidFormatAttributes) super.getAttributes();
  }

  public static final class Builder
      extends ValidationError.Builder<InvalidFormatAttributes, InvalidFormatValidationError> {

    private Builder() {
    }

    @Override
    public InvalidFormatValidationError build() {
      return new InvalidFormatValidationError(detail, ref, attributes);
    }
  }
}
