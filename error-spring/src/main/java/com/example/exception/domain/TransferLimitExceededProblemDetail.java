package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

/**
 * Problem detail for transfer limit exceeded errors.
 */
public final class TransferLimitExceededProblemDetail extends DomainProblemDetail {

  private static final String CODE = "TRANSFER_LIMIT_EXCEEDED";
  private static final String TITLE = "Transfer Limit Exceeded";

  public TransferLimitExceededProblemDetail() {
    super(CODE, TITLE, null, null);
  }

  public TransferLimitExceededProblemDetail(String detail,
      TransferLimitExceededAttributes attributes) {
    super(CODE, TITLE, detail, attributes);
  }

  public TransferLimitExceededProblemDetail(HttpStatus status, String detail,
      TransferLimitExceededAttributes attributes) {
    super(status, CODE, TITLE, detail, attributes);
  }

  public TransferLimitExceededProblemDetail(String detail, BigDecimal amount, String currency) {
    this(detail, new TransferLimitExceededAttributes(amount, currency));
  }

  @Override
  public TransferLimitExceededAttributes getAttributes() {
    return (TransferLimitExceededAttributes) super.getAttributes();
  }

  @JsonSetter(ATTRIBUTES_PROPERTY)
  private void setAttributes(TransferLimitExceededAttributes attributes) {
    setProperty(ATTRIBUTES_PROPERTY, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends DomainProblemDetail.Builder<TransferLimitExceededAttributes, TransferLimitExceededProblemDetail> {

    private Builder() {
    }

    @Override
    public TransferLimitExceededProblemDetail build() {
      return new TransferLimitExceededProblemDetail(status, detail, attributes);
    }
  }
}
