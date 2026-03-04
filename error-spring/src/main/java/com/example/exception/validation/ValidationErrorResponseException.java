package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;
import java.util.List;

/**
 * Exception for validation errors. Detail is not supported at root level - each ValidationError has
 * its own detail. Sealed to only permit public and internal validation exception categories.
 */
public abstract sealed class ValidationErrorResponseException extends ApiErrorResponseException
    permits PublicValidationErrorResponseException, InternalValidationErrorResponseException {

  protected ValidationErrorResponseException(ValidationProblemDetail problemDetail) {
    super(problemDetail);
  }

  protected ValidationErrorResponseException(ValidationProblemDetail problemDetail,
      Throwable cause) {
    super(problemDetail, cause);
  }

  public ValidationProblemDetail getProblemDetail() {
    return (ValidationProblemDetail) getBody();
  }

  public List<ValidationError> getErrors() {
    return getProblemDetail().getErrors();
  }

  public abstract static class Builder<T extends ValidationErrorResponseException>
      extends ApiErrorResponseException.Builder<ValidationProblemDetail, T> {

    protected Builder() {
    }
  }
}
