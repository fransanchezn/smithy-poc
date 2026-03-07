package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonCreator
  private AccessErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance), ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
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
      return new AccessErrorResponseException(buildProblemDetail(TYPE, title, DEFAULT_STATUS, detail, null));
    }
  }
}
