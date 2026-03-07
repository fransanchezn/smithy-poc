package com.example.exception;

/**
 * Base interface for all error codes.
 */
public interface ErrorCode {

  String getCode();

  default String getDelimiter() {
    return ".";
  }
}
