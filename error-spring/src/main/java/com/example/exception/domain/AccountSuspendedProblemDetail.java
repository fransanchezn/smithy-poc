package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.http.HttpStatus;

/**
 * Problem detail for account suspended errors.
 */
public final class AccountSuspendedProblemDetail extends DomainProblemDetail {

  private static final String CODE = "ACCOUNT_SUSPENDED";
  private static final String TITLE = "Account Suspended";

  public AccountSuspendedProblemDetail() {
    super(CODE, TITLE, null, null);
  }

  public AccountSuspendedProblemDetail(String detail, AccountSuspendedAttributes attributes) {
    super(CODE, TITLE, detail, attributes);
  }

  public AccountSuspendedProblemDetail(HttpStatus status, String detail,
      AccountSuspendedAttributes attributes) {
    super(status, CODE, TITLE, detail, attributes);
  }

  public AccountSuspendedProblemDetail(String detail, String reason) {
    this(detail, new AccountSuspendedAttributes(reason));
  }

  @Override
  public AccountSuspendedAttributes getAttributes() {
    return (AccountSuspendedAttributes) super.getAttributes();
  }

  @JsonSetter(ATTRIBUTES_PROPERTY)
  private void setAttributes(AccountSuspendedAttributes attributes) {
    setProperty(ATTRIBUTES_PROPERTY, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends DomainProblemDetail.Builder<AccountSuspendedAttributes, AccountSuspendedProblemDetail> {

    private Builder() {
    }

    @Override
    public AccountSuspendedProblemDetail build() {
      return new AccountSuspendedProblemDetail(status, detail, attributes);
    }
  }
}
