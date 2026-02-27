package com.example.exception.validation;

import com.fasterxml.jackson.annotation.JsonSetter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Problem detail for validation errors. Detail is not supported at root level - each
 * ValidationError has its own detail.
 */
public final class ValidationProblemDetail extends ProblemDetail {

  private static final URI TYPE = URI.create("/errors/types/validation");
  private static final String TITLE = "Validation Problem";
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String ERRORS_PROPERTY = "errors";

  ValidationProblemDetail() {
    super(STATUS.value());
    setType(TYPE);
    setTitle(TITLE);
    setProperty(ERRORS_PROPERTY, new ArrayList<ValidationError>());
  }

  private ValidationProblemDetail(List<ValidationError> errors) {
    super(STATUS.value());
    setType(TYPE);
    setTitle(TITLE);
    setProperty(ERRORS_PROPERTY, new ArrayList<>(errors));
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public List<ValidationError> getErrors() {
    return (List<ValidationError>) Optional.ofNullable(getProperties())
        .map(it -> it.get(ERRORS_PROPERTY))
        .orElseGet(ArrayList::new);
  }

  @JsonSetter(ERRORS_PROPERTY)
  private void setErrors(List<ValidationError> errors) {
    getErrors().clear();
    getErrors().addAll(errors);
  }

  public static final class Builder {

    private final List<ValidationError> errors = new ArrayList<>();

    private Builder() {
    }

    public Builder error(ValidationError error) {
      this.errors.add(error);
      return this;
    }

    public Builder errors(List<? extends ValidationError> errors) {
      this.errors.addAll(errors);
      return this;
    }

    public ValidationProblemDetail build() {
      return new ValidationProblemDetail(errors);
    }
  }
}
