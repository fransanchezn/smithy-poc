package com.example.exception.validation;

import com.example.exception.ErrorAttributes;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type-safe attributes for invalid format validation errors.
 */
public record InvalidFormatAttributes(String pattern) implements ErrorAttributes {

  @JsonCreator
  public InvalidFormatAttributes(@JsonProperty("pattern") String pattern) {
    this.pattern = pattern;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String pattern;

    private Builder() {
    }

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public InvalidFormatAttributes build() {
      return new InvalidFormatAttributes(pattern);
    }
  }
}
