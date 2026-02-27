package com.example.exception.validation;

import com.example.exception.ErrorAttributes;

/**
 * Type-safe attributes for invalid format validation errors.
 */
public record InvalidFormatAttributes(String pattern) implements ErrorAttributes {

}
