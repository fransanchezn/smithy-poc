package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;

/**
 * Exception for access-related errors (authentication, authorization). Sealed to only permit public
 * and internal access exception categories.
 */
public abstract sealed class AccessErrorResponseException extends ApiErrorResponseException
    permits PublicAccessErrorResponseException, InternalAccessErrorResponseException {

  protected AccessErrorResponseException(AccessProblemDetail problemDetail) {
    super(problemDetail);
  }

  protected AccessErrorResponseException(AccessProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public AccessProblemDetail getProblemDetail() {
    return (AccessProblemDetail) getBody();
  }

  public abstract static class Builder<T extends AccessErrorResponseException>
      extends ApiErrorResponseException.Builder<AccessProblemDetail, T> {

    protected Builder() {
    }
  }
}
