package com.example.exception.domain;

/**
 * Exception for public domain-specific business errors that are exposed to API consumers. Sealed to
 * only permit specific public domain exception implementations.
 */
public abstract sealed class PublicDomainErrorResponseException extends DomainErrorResponseException
    permits TransferLimitExceededException, AccountSuspendedException {

  protected PublicDomainErrorResponseException(DomainProblemDetail problemDetail) {
    super(problemDetail);
  }

  protected PublicDomainErrorResponseException(DomainProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public abstract static class Builder<P extends DomainProblemDetail, T extends PublicDomainErrorResponseException>
      extends DomainErrorResponseException.Builder<P, T> {

    protected Builder() {
    }
  }
}
