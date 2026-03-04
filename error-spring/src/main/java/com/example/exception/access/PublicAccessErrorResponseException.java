package com.example.exception.access;

/**
 * Exception for public access-related errors that are exposed to API consumers.
 */
public non-sealed class PublicAccessErrorResponseException extends AccessErrorResponseException {

  private PublicAccessErrorResponseException(AccessProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends AccessErrorResponseException.Builder<PublicAccessErrorResponseException> {

    private Builder() {
    }

    @Override
    public PublicAccessErrorResponseException build() {
      return new PublicAccessErrorResponseException(problemDetail, cause);
    }
  }
}
