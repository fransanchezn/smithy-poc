package com.example.exception.domain;

import com.example.exception.ErrorAttributes;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Problem detail for domain-specific business errors. Sealed to only permit specific domain problem
 * detail implementations.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "code")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TransferLimitExceededProblemDetail.class, name = "TRANSFER_LIMIT_EXCEEDED"),
    @JsonSubTypes.Type(value = AccountSuspendedProblemDetail.class, name = "ACCOUNT_SUSPENDED")
})
public abstract sealed class DomainProblemDetail extends ProblemDetail
    permits TransferLimitExceededProblemDetail, AccountSuspendedProblemDetail {

  protected static final String ATTRIBUTES_PROPERTY = "attributes";
  private static final URI TYPE = URI.create("/errors/types/domain");
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
  private static final String CODE_PROPERTY = "code";

  protected DomainProblemDetail(String code, String title, String detail,
      ErrorAttributes attributes) {
    this(DEFAULT_STATUS, code, title, detail, attributes);
  }

  protected DomainProblemDetail(HttpStatus status, String code, String title, String detail,
      ErrorAttributes attributes) {
    super(status.value());
    setType(TYPE);
    setTitle(title);
    if (detail != null) {
      setDetail(detail);
    }
    setProperty(CODE_PROPERTY, code);
    setProperty(ATTRIBUTES_PROPERTY, attributes);
  }

  public String getCode() {
    return (String) Optional.ofNullable(getProperties())
        .map(it -> it.get(CODE_PROPERTY))
        .orElse(null);
  }

  public ErrorAttributes getAttributes() {
    return (ErrorAttributes) Optional.ofNullable(getProperties())
        .map(it -> it.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  public abstract static class Builder<A extends ErrorAttributes, T extends DomainProblemDetail> {

    protected HttpStatus status = DEFAULT_STATUS;
    protected String detail;
    protected A attributes;

    protected Builder() {
    }

    public Builder<A, T> status(HttpStatus status) {
      this.status = status;
      return this;
    }

    public Builder<A, T> detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder<A, T> attributes(A attributes) {
      this.attributes = attributes;
      return this;
    }

    public abstract T build();
  }
}
