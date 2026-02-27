package com.example.exception.validation;

import com.example.exception.ErrorAttributes;

/**
 * Type-safe attributes for invalid format validation errors.
 */
public record InvalidFormatAttributes(String pattern) implements ErrorAttributes {

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
