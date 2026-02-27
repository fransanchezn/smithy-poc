package com.example.exception.domain;

/**
 * Error codes for account-related domain errors.
 */
public enum AccountErrorCode implements DomainErrorCode {

  ACCOUNT_SUSPENDED("account_suspended");

  private static final String DOMAIN = "account";

  private final String errorCode;
  private final String code;

  AccountErrorCode(String errorCode) {
    this.errorCode = errorCode;
    this.code = DOMAIN + "." + errorCode;
  }

  @Override
  public String getDomain() {
    return DOMAIN;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }
}
