package com.example.exception.validation;

/**
 * Exception for internal validation errors that are not exposed to API consumers. These errors are
 * for internal use only and should be handled/transformed before reaching the API boundary.
 */
public non-sealed class InternalValidationErrorResponseException
    extends ValidationErrorResponseException {

  private InternalValidationErrorResponseException(ValidationProblemDetail problemDetail,
      Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ValidationErrorResponseException.Builder<InternalValidationErrorResponseException> {

    private Builder() {
    }

    @Override
    public InternalValidationErrorResponseException build() {
      return new InternalValidationErrorResponseException(problemDetail, cause);
    }
  }
}
