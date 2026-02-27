package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;
import java.util.List;

/**
 * Exception for validation errors. Detail is not supported at root level - each ValidationError has
 * its own detail.
 */
public final class ValidationErrorResponseException extends ApiErrorResponseException {

  private ValidationErrorResponseException(ValidationProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public ValidationProblemDetail getProblemDetail() {
    return (ValidationProblemDetail) getBody();
  }

  public List<ValidationError> getErrors() {
    return getProblemDetail().getErrors();
  }

  public static final class Builder
      extends ApiErrorResponseException.Builder<ValidationProblemDetail, ValidationErrorResponseException> {

    private Builder() {
    }

    @Override
    public ValidationErrorResponseException build() {
      return new ValidationErrorResponseException(problemDetail, cause);
    }
  }
}
