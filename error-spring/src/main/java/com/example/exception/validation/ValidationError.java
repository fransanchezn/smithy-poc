package com.example.exception.validation;

import com.example.exception.ErrorAttributes;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base class for validation error details. Sealed to only permit specific validation error
 * implementations. Each validation error has a code, detail message, ref (field reference), and
 * type-safe attributes.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "code")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InvalidFormatValidationError.class, name = "invalid_format"),
    @JsonSubTypes.Type(value = MissingValueValidationError.class, name = "missing_value")
})
public abstract sealed class ValidationError
    permits InvalidFormatValidationError, MissingValueValidationError {

  private final String code;
  private final String detail;
  private final String ref;
  private final ErrorAttributes attributes;

  protected ValidationError(String code, String detail, String ref, ErrorAttributes attributes) {
    this.code = code;
    this.detail = detail;
    this.ref = ref;
    this.attributes = attributes;
  }

  public String getCode() {
    return code;
  }

  public String getDetail() {
    return detail;
  }

  public String getRef() {
    return ref;
  }

  public ErrorAttributes getAttributes() {
    return attributes;
  }
}
