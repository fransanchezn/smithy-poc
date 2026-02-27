package com.example.exception.domain;

import com.example.exception.ErrorAttributes;

/**
 * Type-safe attributes for account suspended errors.
 */
public record AccountSuspendedAttributes(String reason) implements ErrorAttributes {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String reason;

    private Builder() {
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public AccountSuspendedAttributes build() {
      return new AccountSuspendedAttributes(reason);
    }
  }
}
