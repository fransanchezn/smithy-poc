package com.example.exception;

import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Base exception for all API errors that follow RFC 7807 Problem Details. Extends Spring Boot's
 * ErrorResponseException to integrate with Spring's error handling.
 */
public abstract class ApiErrorResponseException extends ErrorResponseException {

  public static final String ERROR_TYPE_HEADER = "x-error-type";

  protected ApiErrorResponseException(ProblemDetail problemDetail, String errorType) {
    super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, null);
    getHeaders().add(ERROR_TYPE_HEADER, errorType);
  }

  public URI getType() {
    return getBody().getType();
  }

  public String getTitle() {
    return getBody().getTitle();
  }

  public int getStatus() {
    return getBody().getStatus();
  }

  public String getDetail() {
    return getBody().getDetail();
  }

  public URI getInstance() {
    return getBody().getInstance();
  }
}
