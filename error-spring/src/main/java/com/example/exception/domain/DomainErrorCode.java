package com.example.exception.domain;

import com.example.exception.ErrorCode;

/**
 * Base interface for domain error codes. Follows the convention {@code <domain>.<error_code>}.
 */
public sealed interface DomainErrorCode extends ErrorCode
    permits TransferErrorCode, AccountErrorCode {

  String getDomain();

  String getErrorCode();
}
