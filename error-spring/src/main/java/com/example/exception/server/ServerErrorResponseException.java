package com.example.exception.server;

import com.example.exception.ApiErrorResponseException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception for server-side errors (internal errors, service unavailable).
 */
public final class ServerErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "ServerErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/server");
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

  private ServerErrorResponseException(ProblemDetail problemDetail) {
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

    public ServerErrorResponseException build() {
      return new ServerErrorResponseException(buildProblemDetail(title, detail));
    }
  }
}
