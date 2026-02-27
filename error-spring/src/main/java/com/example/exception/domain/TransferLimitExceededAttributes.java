package com.example.exception.domain;

import com.example.exception.ErrorAttributes;
import java.math.BigDecimal;

/**
 * Type-safe attributes for transfer limit exceeded errors.
 */
public record TransferLimitExceededAttributes(BigDecimal amount, String currency) implements
    ErrorAttributes {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private BigDecimal amount;
    private String currency;

    private Builder() {
    }

    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public TransferLimitExceededAttributes build() {
      return new TransferLimitExceededAttributes(amount, currency);
    }
  }
}
