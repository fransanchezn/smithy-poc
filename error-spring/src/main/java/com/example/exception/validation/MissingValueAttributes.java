package com.example.exception.validation;

import com.example.exception.ErrorAttributes;

/**
 * Type-safe attributes for missing value validation errors.
 */
public record MissingValueAttributes(String missingField) implements ErrorAttributes {

}
