package com.example.exception.domain;

/**
 * Exception thrown when an account is suspended.
 */
public final class AccountSuspendedException extends DomainErrorResponseException {

  public AccountSuspendedException(AccountSuspendedProblemDetail problemDetail) {
    super(problemDetail);
  }

  public AccountSuspendedException(AccountSuspendedProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public AccountSuspendedException(String detail, AccountSuspendedAttributes attributes) {
    super(new AccountSuspendedProblemDetail(detail, attributes));
  }

  public AccountSuspendedException(String detail, String reason) {
    super(new AccountSuspendedProblemDetail(detail, reason));
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public AccountSuspendedProblemDetail getProblemDetail() {
    return (AccountSuspendedProblemDetail) super.getProblemDetail();
  }

  @Override
  public AccountSuspendedAttributes getAttributes() {
    return getProblemDetail().getAttributes();
  }

  public static final class Builder
      extends DomainErrorResponseException.Builder<AccountSuspendedProblemDetail, AccountSuspendedException> {

    private Builder() {
    }

    @Override
    public AccountSuspendedException build() {
      return new AccountSuspendedException(problemDetail, cause);
    }
  }
}
