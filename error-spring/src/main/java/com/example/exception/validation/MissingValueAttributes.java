package com.example.exception.validation;

import com.example.exception.ErrorAttributes;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type-safe attributes for missing value validation errors.
 */
public record MissingValueAttributes(String missingField) implements ErrorAttributes {

  @JsonCreator
  public MissingValueAttributes(@JsonProperty("missingField") String missingField) {
    this.missingField = missingField;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String missingField;

    private Builder() {
    }

    public Builder missingField(String missingField) {
      this.missingField = missingField;
      return this;
    }

    public MissingValueAttributes build() {
      return new MissingValueAttributes(missingField);
    }
  }
}
