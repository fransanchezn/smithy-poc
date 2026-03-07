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

  public static AccountErrorCode valueOfCode(String code) {
    for (AccountErrorCode errorCode : values()) {
      if (errorCode.getCode().equals(code)) {
        return errorCode;
      }
    }
    throw new IllegalArgumentException("No enum constant with code: " + code);
  }
}
