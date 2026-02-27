package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;

/**
 * Exception for access-related errors (authentication, authorization).
 */
public final class AccessErrorResponseException extends ApiErrorResponseException {

  public AccessErrorResponseException(AccessProblemDetail problemDetail) {
    super(problemDetail);
  }

  public AccessErrorResponseException(AccessProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static AccessErrorResponseException unauthorized(String detail) {
    return new AccessErrorResponseException(AccessProblemDetail.unauthorized(detail));
  }

  public static AccessErrorResponseException forbidden(String detail) {
    return new AccessErrorResponseException(AccessProblemDetail.forbidden(detail));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ApiErrorResponseException.Builder<AccessProblemDetail, AccessErrorResponseException> {

    private Builder() {
    }

    @Override
    public AccessErrorResponseException build() {
      return new AccessErrorResponseException(problemDetail, cause);
    }
  }
}
