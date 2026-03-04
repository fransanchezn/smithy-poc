package com.example.exception.access;

/**
 * Exception for internal access-related errors that are not exposed to API consumers. These errors
 * are for internal use only and should be handled/transformed before reaching the API boundary.
 */
public non-sealed class InternalAccessErrorResponseException extends AccessErrorResponseException {

  private InternalAccessErrorResponseException(AccessProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends AccessErrorResponseException.Builder<InternalAccessErrorResponseException> {

    private Builder() {
    }

    @Override
    public InternalAccessErrorResponseException build() {
      return new InternalAccessErrorResponseException(problemDetail, cause);
    }
  }
}
