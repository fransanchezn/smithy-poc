package com.example.exception.domain;

import com.example.exception.ApiErrorResponseException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception thrown when a transfer exceeds the allowed limit.
 */
public final class TransferLimitExceededException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "TransferLimitExceededException";
  private static final URI TYPE = URI.create("/errors/types/domain");
  private static final TransferErrorCode CODE = TransferErrorCode.TRANSFER_LIMIT_EXCEEDED;
  private static final String TITLE = "Transfer Limit Exceeded";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
  private static final String CODE_PROPERTY = "code";
  private static final String ATTRIBUTES_PROPERTY = "attributes";

  private TransferLimitExceededException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private TransferLimitExceededException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") String code,
      @JsonProperty("attributes") TransferLimitExceededAttributes attributes) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance, TransferErrorCode.valueOfCode(code), attributes),
        ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  public TransferErrorCode getCode() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (TransferErrorCode) props.get(CODE_PROPERTY))
        .orElse(CODE);
  }

  public TransferLimitExceededAttributes getAttributes() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (TransferLimitExceededAttributes) props.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, TransferErrorCode code, TransferLimitExceededAttributes attributes) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title != null ? title : TITLE);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(CODE_PROPERTY, code != null ? code : CODE.getCode());
    problemDetail.setProperty(ATTRIBUTES_PROPERTY, attributes);
    return problemDetail;
  }

  public static final class Builder {

    private String detail;
    private TransferLimitExceededAttributes attributes;

    private Builder() {
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder attributes(TransferLimitExceededAttributes attributes) {
      this.attributes = attributes;
      return this;
    }

    public TransferLimitExceededException build() {
      Objects.requireNonNull(attributes, "attributes is required");
      return new TransferLimitExceededException(buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, detail, null, CODE, attributes));
    }
  }
}
