package com.example.exception.domain;

import com.example.exception.ApiErrorResponseException;
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

  public static Builder builder() {
    return new Builder();
  }

  public String getCode() {
    return CODE.getCode();
  }

  public AccountSuspendedAttributes getAttributes() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (AccountSuspendedAttributes) props.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  private static ProblemDetail buildProblemDetail(String detail,
      AccountSuspendedAttributes attributes) {
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
      return new AccountSuspendedException(buildProblemDetail(detail, attributes));
    }
  }
}
