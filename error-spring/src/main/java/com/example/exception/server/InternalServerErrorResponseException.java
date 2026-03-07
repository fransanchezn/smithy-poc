package com.example.exception.server;

import com.example.exception.ApiErrorResponseException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception for server-side errors (internal errors, service unavailable).
 */
public final class InternalServerErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "InternalServerErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/server");
  private static final String TITLE = "Internal Server Error";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

  private InternalServerErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private InternalServerErrorResponseException(
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

    private String detail;

    private Builder() {
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public InternalServerErrorResponseException build() {
      return new InternalServerErrorResponseException(buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, detail, null));
    }
  }
}
