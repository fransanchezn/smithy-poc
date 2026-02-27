package com.example.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Base exception for all API errors that follow RFC 7807 Problem Details. Extends Spring Boot's
 * ErrorResponseException to integrate with Spring's error handling.
 */
public abstract class ApiErrorResponseException extends ErrorResponseException {

  protected ApiErrorResponseException(ProblemDetail problemDetail) {
    super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, null);
  }

  protected ApiErrorResponseException(ProblemDetail problemDetail, Throwable cause) {
    super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, cause);
  }

  protected abstract static class Builder<P extends ProblemDetail, T extends ApiErrorResponseException> {

    protected P problemDetail;
    protected Throwable cause;

    protected Builder() {
    }

    public Builder<P, T> problemDetail(P problemDetail) {
      this.problemDetail = problemDetail;
      return this;
    }

    public Builder<P, T> cause(Throwable cause) {
      this.cause = cause;
      return this;
    }

    public abstract T build();
  }
}
