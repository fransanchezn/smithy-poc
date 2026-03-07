package com.example.exception.validation;

import com.example.exception.ApiErrorResponseException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonCreator
  private ValidationErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("errors") List<ValidationError> errors) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance, errors), ERROR_TYPE);
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

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, List<ValidationError> errors) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title != null ? title : TITLE);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(ERRORS_PROPERTY,
        errors != null ? new ArrayList<>(errors) : new ArrayList<>());
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
      return new ValidationErrorResponseException(buildProblemDetail(TYPE, TITLE, STATUS, null, null, errors));
    }
  }
}
