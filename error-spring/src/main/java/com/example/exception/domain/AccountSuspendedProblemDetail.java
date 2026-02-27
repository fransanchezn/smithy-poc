package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.http.HttpStatus;

/**
 * Problem detail for account suspended errors.
 */
public final class AccountSuspendedProblemDetail extends DomainProblemDetail {

  private static final AccountErrorCode CODE = AccountErrorCode.ACCOUNT_SUSPENDED;
  private static final String TITLE = "Account Suspended";

  AccountSuspendedProblemDetail() {
    super(CODE.getCode(), TITLE, null, null);
  }

  private AccountSuspendedProblemDetail(HttpStatus status, String detail,
      AccountSuspendedAttributes attributes) {
    super(status, CODE.getCode(), TITLE, detail, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public AccountSuspendedAttributes getAttributes() {
    return (AccountSuspendedAttributes) super.getAttributes();
  }

  @JsonSetter(ATTRIBUTES_PROPERTY)
  private void setAttributes(AccountSuspendedAttributes attributes) {
    setProperty(ATTRIBUTES_PROPERTY, attributes);
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
