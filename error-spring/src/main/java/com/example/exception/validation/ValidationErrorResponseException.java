package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Exception for validation errors. Detail is not supported at root level - each ValidationError has
 * its own detail.
 */
public final class ValidationErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "ValidationErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/validation");
  private static final String TITLE = "Validation Problem";
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String ERRORS_PROPERTY = "errors";

  private ValidationErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public List<ValidationError> getErrors() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (List<ValidationError>) props.get(ERRORS_PROPERTY))
        .orElse(Collections.emptyList());
  }

  private static ProblemDetail buildProblemDetail(List<ValidationError> errors) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(STATUS);
    problemDetail.setType(TYPE);
    problemDetail.setTitle(TITLE);
    problemDetail.setProperty(ERRORS_PROPERTY, new ArrayList<>(errors));
    return problemDetail;
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

    public ValidationErrorResponseException build() {
      return new ValidationErrorResponseException(buildProblemDetail(errors));
    }
  }
}
