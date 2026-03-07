package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception for access-related errors (authentication, authorization).
 */
public final class UnauthorizedAccessErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "UnauthorizedAccessErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/access");
  private static final AccessErrorCode CODE = AccessErrorCode.UNAUTHORIZED;
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNAUTHORIZED;
  private static final String CODE_PROPERTY = "code";

  private UnauthorizedAccessErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private UnauthorizedAccessErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") String code) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance,
        code != null ? AccessErrorCode.valueOfCode(code) : CODE), ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  public AccessErrorCode getCode() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (AccessErrorCode) props.get(CODE_PROPERTY))
        .orElse(CODE);
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, AccessErrorCode code) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(CODE_PROPERTY, code != null ? code : CODE);
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

    public UnauthorizedAccessErrorResponseException build() {
      return new UnauthorizedAccessErrorResponseException(buildProblemDetail(TYPE, title, DEFAULT_STATUS, detail, null, CODE));
    }
  }
}
