package com.example.exception.domain;

import java.math.BigDecimal;

/**
 * Exception thrown when a transfer exceeds the allowed limit.
 */
public final class TransferLimitExceededException extends DomainErrorResponseException {

  public TransferLimitExceededException(TransferLimitExceededProblemDetail problemDetail) {
    super(problemDetail);
  }

  public TransferLimitExceededException(TransferLimitExceededProblemDetail problemDetail,
      Throwable cause) {
    super(problemDetail, cause);
  }

  public TransferLimitExceededException(String detail, TransferLimitExceededAttributes attributes) {
    super(new TransferLimitExceededProblemDetail(detail, attributes));
  }

  public TransferLimitExceededException(String detail, BigDecimal amount, String currency) {
    super(new TransferLimitExceededProblemDetail(detail, amount, currency));
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public TransferLimitExceededProblemDetail getProblemDetail() {
    return (TransferLimitExceededProblemDetail) super.getProblemDetail();
  }

  @Override
  public TransferLimitExceededAttributes getAttributes() {
    return getProblemDetail().getAttributes();
  }

  public static final class Builder
      extends DomainErrorResponseException.Builder<TransferLimitExceededProblemDetail, TransferLimitExceededException> {

    private Builder() {
    }

    @Override
    public TransferLimitExceededException build() {
      return new TransferLimitExceededException(problemDetail, cause);
    }
  }
}
