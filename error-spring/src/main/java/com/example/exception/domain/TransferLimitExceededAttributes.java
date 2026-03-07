package com.example.exception.domain;

import com.example.exception.ErrorAttributes;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Type-safe attributes for transfer limit exceeded errors.
 */
public record TransferLimitExceededAttributes(BigDecimal amount, String currency) implements
    ErrorAttributes {

  @JsonCreator
  public TransferLimitExceededAttributes(
      @JsonProperty("amount") BigDecimal amount,
      @JsonProperty("currency") String currency) {
    this.amount = amount;
    this.currency = currency;
  }

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
