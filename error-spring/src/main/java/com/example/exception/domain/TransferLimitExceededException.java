package com.example.exception.domain;

import com.example.exception.ApiErrorResponseException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception thrown when a transfer exceeds the allowed limit.
 */
public final class TransferLimitExceededException extends ApiErrorResponseException {

  private static final URI TYPE = URI.create("/errors/types/domain");
  private static final TransferErrorCode CODE = TransferErrorCode.TRANSFER_LIMIT_EXCEEDED;
  private static final String TITLE = "Transfer Limit Exceeded";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
  private static final String CODE_PROPERTY = "code";
  private static final String ATTRIBUTES_PROPERTY = "attributes";

  private TransferLimitExceededException(ProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getCode() {
    return CODE.getCode();
  }

  public TransferLimitExceededAttributes getAttributes() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (TransferLimitExceededAttributes) props.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  private static ProblemDetail buildProblemDetail(String detail,
      TransferLimitExceededAttributes attributes) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(DEFAULT_STATUS);
    problemDetail.setType(TYPE);
    problemDetail.setTitle(TITLE);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    problemDetail.setProperty(CODE_PROPERTY, CODE.getCode());
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
      return new TransferLimitExceededException(buildProblemDetail(detail, attributes), null);
    }
  }
}
