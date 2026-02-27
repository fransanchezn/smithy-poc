package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;
import java.util.List;

/**
 * Exception for validation errors. Detail is not supported at root level - each ValidationError has
 * its own detail.
 */
public final class ValidationErrorResponseException extends ApiErrorResponseException {

  public ValidationErrorResponseException() {
    super(new ValidationProblemDetail());
  }

  public ValidationErrorResponseException(ValidationProblemDetail problemDetail) {
    super(problemDetail);
  }

  public ValidationErrorResponseException(ValidationProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public ValidationErrorResponseException(List<? extends ValidationError> errors) {
    super(new ValidationProblemDetail(errors));
  }

  // Factory methods

  public static ValidationErrorResponseException invalidFormat(String detail, String ref,
      String pattern) {
    return new ValidationErrorResponseException(
        ValidationProblemDetail.invalidFormat(detail, ref, pattern));
  }

  public static ValidationErrorResponseException missingValue(String detail, String ref,
      String missingField) {
    return new ValidationErrorResponseException(
        ValidationProblemDetail.missingValue(detail, ref, missingField));
  }

  public static Builder builder() {
    return new Builder();
  }

  public ValidationProblemDetail getProblemDetail() {
    return (ValidationProblemDetail) getBody();
  }

  public ValidationErrorResponseException addError(ValidationError error) {
    getProblemDetail().addError(error);
    return this;
  }

  public ValidationErrorResponseException addInvalidFormat(String detail, String ref,
      String pattern) {
    getProblemDetail().addInvalidFormat(detail, ref, pattern);
    return this;
  }

  public ValidationErrorResponseException addMissingValue(String detail, String ref,
      String missingField) {
    getProblemDetail().addMissingValue(detail, ref, missingField);
    return this;
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
