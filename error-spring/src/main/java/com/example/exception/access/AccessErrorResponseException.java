package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception for access-related errors (authentication, authorization).
 */
public final class AccessErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "AccessErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/access");
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNAUTHORIZED;

  private AccessErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  private static ProblemDetail buildProblemDetail(String title, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(DEFAULT_STATUS);
    problemDetail.setType(TYPE);
    problemDetail.setTitle(title);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    return problemDetail;
  }

  public static final class Builder {

    private String title;
    private String detail;

    private Builder() {
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public AccessErrorResponseException build() {
      return new AccessErrorResponseException(buildProblemDetail(title, detail));
    }
  }
}
