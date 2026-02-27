package com.example.exception.domain;

import com.example.exception.ErrorCode;

/**
 * Base interface for domain error codes. Follows the convention {@code <domain>.<error_code>}.
 */
public interface DomainErrorCode extends ErrorCode {

  String getDomain();

  String getErrorCode();
}
