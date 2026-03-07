package com.example.exception.access;

import com.example.exception.ErrorCode;

/**
 * Error codes for access-related errors (authentication, authorization).
 */
public enum AccessErrorCode implements ErrorCode {

  UNAUTHORIZED("unauthorized");

  private final String code;

  AccessErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }

  public static AccessErrorCode valueOfCode(String code) {
    for (AccessErrorCode errorCode : values()) {
      if (errorCode.getCode().equals(code)) {
        return errorCode;
      }
    }
    throw new IllegalArgumentException("No enum constant with code: " + code);
  }
}
