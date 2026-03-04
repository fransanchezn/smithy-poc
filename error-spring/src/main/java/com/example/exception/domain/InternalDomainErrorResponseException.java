package com.example.exception.domain;

/**
 * Exception for internal domain-specific business errors that are not exposed to API consumers.
 * These errors are for internal use only and should be handled/transformed before reaching the API
 * boundary.
 */
public non-sealed class InternalDomainErrorResponseException extends DomainErrorResponseException {

  private InternalDomainErrorResponseException(DomainProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends DomainErrorResponseException.Builder<DomainProblemDetail, InternalDomainErrorResponseException> {

    private Builder() {
    }

    @Override
    public InternalDomainErrorResponseException build() {
      return new InternalDomainErrorResponseException(problemDetail, cause);
    }
  }
}
