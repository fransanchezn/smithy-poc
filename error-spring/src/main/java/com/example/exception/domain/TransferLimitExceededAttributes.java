package com.example.exception.domain;

import com.example.exception.ErrorAttributes;
import java.math.BigDecimal;

/**
 * Type-safe attributes for transfer limit exceeded errors.
 */
public record TransferLimitExceededAttributes(BigDecimal amount, String currency) implements
    ErrorAttributes {

}
