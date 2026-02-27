package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.http.HttpStatus;

/**
 * Problem detail for transfer limit exceeded errors.
 */
public final class TransferLimitExceededProblemDetail extends DomainProblemDetail {

  private static final TransferErrorCode CODE = TransferErrorCode.TRANSFER_LIMIT_EXCEEDED;
  private static final String TITLE = "Transfer Limit Exceeded";

  TransferLimitExceededProblemDetail() {
    super(CODE.getCode(), TITLE, null, null);
  }

  private TransferLimitExceededProblemDetail(HttpStatus status, String detail,
      TransferLimitExceededAttributes attributes) {
    super(status, CODE.getCode(), TITLE, detail, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public TransferLimitExceededAttributes getAttributes() {
    return (TransferLimitExceededAttributes) super.getAttributes();
  }

  @JsonSetter(ATTRIBUTES_PROPERTY)
  private void setAttributes(TransferLimitExceededAttributes attributes) {
    setProperty(ATTRIBUTES_PROPERTY, attributes);
  }

  public static final class Builder
      extends DomainProblemDetail.Builder<TransferLimitExceededAttributes, TransferLimitExceededProblemDetail> {

    private Builder() {
    }

    @Override
    public TransferLimitExceededProblemDetail build() {
      return new TransferLimitExceededProblemDetail(status, detail, attributes);
    }
  }
}
