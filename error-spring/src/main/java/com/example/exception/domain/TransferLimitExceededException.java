package com.example.exception.domain;

import java.math.BigDecimal;

/**
 * Exception thrown when a transfer exceeds the allowed limit.
 */
public final class TransferLimitExceededException extends DomainErrorResponseException {

  public TransferLimitExceededException(TransferLimitExceededProblemDetail problemDetail) {
    super(problemDetail);
  }

  public TransferLimitExceededException(String detail, TransferLimitExceededAttributes attributes) {
    super(new TransferLimitExceededProblemDetail(detail, attributes));
  }

  public TransferLimitExceededException(String detail, BigDecimal amount, String currency) {
    super(new TransferLimitExceededProblemDetail(detail, amount, currency));
  }

  @Override
  public TransferLimitExceededProblemDetail getProblemDetail() {
    return (TransferLimitExceededProblemDetail) super.getProblemDetail();
  }

  @Override
  public TransferLimitExceededAttributes getAttributes() {
    return getProblemDetail().getAttributes();
  }
}
