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
 * Exception thrown when an account is suspended.
 */
public final class AccountSuspendedException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "AccountSuspendedException";
  private static final URI TYPE = URI.create("/errors/types/domain");
  private static final AccountErrorCode CODE = AccountErrorCode.ACCOUNT_SUSPENDED;
  private static final String TITLE = "Account Suspended";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
  private static final String CODE_PROPERTY = "code";
  private static final String ATTRIBUTES_PROPERTY = "attributes";

  private AccountSuspendedException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private AccountSuspendedException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") String code,
      @JsonProperty("attributes") AccountSuspendedAttributes attributes) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance, AccountErrorCode.valueOfCode(code), attributes),
        ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  public AccountErrorCode getCode() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (AccountErrorCode) props.get(CODE_PROPERTY))
        .orElse(CODE);
  }

  public AccountSuspendedAttributes getAttributes() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (AccountSuspendedAttributes) props.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, AccountErrorCode code, AccountSuspendedAttributes attributes) {
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
    private AccountSuspendedAttributes attributes;

    private Builder() {
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder attributes(AccountSuspendedAttributes attributes) {
      this.attributes = attributes;
      return this;
    }

    public AccountSuspendedException build() {
      Objects.requireNonNull(attributes, "attributes is required");
      return new AccountSuspendedException(buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, detail, null, CODE, attributes));
    }
  }
}
