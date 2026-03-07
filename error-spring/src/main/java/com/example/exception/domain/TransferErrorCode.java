package com.example.exception.domain;

/**
 * Error codes for transfer-related domain errors.
 */
public enum TransferErrorCode implements DomainErrorCode {

  TRANSFER_LIMIT_EXCEEDED("transfer_limit_exceeded");

  private static final String DOMAIN = "transfer";

  private final String errorCode;
  private final String code;

  TransferErrorCode(String errorCode) {
    this.errorCode = errorCode;
    this.code = String.join(getDelimiter(), DOMAIN, errorCode);
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

  public static TransferErrorCode valueOfCode(String code) {
    for (TransferErrorCode errorCode : values()) {
      if (errorCode.getCode().equals(code)) {
        return errorCode;
      }
    }
    throw new IllegalArgumentException("No enum constant with code: " + code);
  }
}
