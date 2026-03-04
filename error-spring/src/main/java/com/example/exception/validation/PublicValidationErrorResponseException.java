package com.example.exception.validation;

/**
 * Exception for public validation errors that are exposed to API consumers.
 */
public non-sealed class PublicValidationErrorResponseException
    extends ValidationErrorResponseException {

  private PublicValidationErrorResponseException(ValidationProblemDetail problemDetail,
      Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ValidationErrorResponseException.Builder<PublicValidationErrorResponseException> {

    private Builder() {
    }

    @Override
    public PublicValidationErrorResponseException build() {
      return new PublicValidationErrorResponseException(problemDetail, cause);
    }
  }
}
