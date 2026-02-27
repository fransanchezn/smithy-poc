package com.example.exception.domain;

import com.example.exception.ErrorAttributes;

/**
 * Type-safe attributes for account suspended errors.
 */
public record AccountSuspendedAttributes(String reason) implements ErrorAttributes {

}
