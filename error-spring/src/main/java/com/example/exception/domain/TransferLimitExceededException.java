package com.example.exception.domain;

/**
 * Exception thrown when a transfer exceeds the allowed limit.
 */
public final class TransferLimitExceededException extends DomainErrorResponseException {

  private TransferLimitExceededException(TransferLimitExceededProblemDetail problemDetail,
      Throwable cause) {
    super(problemDetail, cause);
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
