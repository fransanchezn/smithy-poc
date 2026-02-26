package com.example.exception.validation;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Problem detail for validation errors.
 * Detail is not supported at root level - each ValidationError has its own detail.
 */
public final class ValidationProblemDetail extends ProblemDetail {

    private static final URI TYPE = URI.create("/errors/types/validation");
    private static final String TITLE = "Validation Problem";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String ERRORS_PROPERTY = "errors";

    public ValidationProblemDetail() {
        super(STATUS.value());
        setType(TYPE);
        setTitle(TITLE);
        setProperty(ERRORS_PROPERTY, new ArrayList<ValidationError>());
    }

    public ValidationProblemDetail(List<? extends ValidationError> errors) {
        super(STATUS.value());
        setType(TYPE);
        setTitle(TITLE);
        setProperty(ERRORS_PROPERTY, new ArrayList<>(errors));
    }

    // Factory methods for InvalidFormatValidationError

    public static ValidationProblemDetail invalidFormat(String detail, String ref, String pattern) {
        return new ValidationProblemDetail()
                .addError(new InvalidFormatValidationError(detail, ref, pattern));
    }

    public static ValidationProblemDetail invalidFormat(String detail, String ref, InvalidFormatAttributes attributes) {
        return new ValidationProblemDetail()
                .addError(new InvalidFormatValidationError(detail, ref, attributes));
    }

    // Factory methods for MissingValueValidationError

    public static ValidationProblemDetail missingValue(String detail, String ref, String missingField) {
        return new ValidationProblemDetail()
                .addError(new MissingValueValidationError(detail, ref, missingField));
    }

    public static ValidationProblemDetail missingValue(String detail, String ref, MissingValueAttributes attributes) {
        return new ValidationProblemDetail()
                .addError(new MissingValueValidationError(detail, ref, attributes));
    }

    public ValidationProblemDetail addError(ValidationError error) {
        getErrors().add(error);
        return this;
    }

    public ValidationProblemDetail addInvalidFormat(String detail, String ref, String pattern) {
        return addError(new InvalidFormatValidationError(detail, ref, pattern));
    }

    public ValidationProblemDetail addMissingValue(String detail, String ref, String missingField) {
        return addError(new MissingValueValidationError(detail, ref, missingField));
    }

    private void clearErrors() {
        getErrors().clear();
    }

    @JsonSetter(ERRORS_PROPERTY)
    private void setErrors(List<ValidationError> errors) {
      clearErrors();
      for (ValidationError error : errors) {
        addError(error);
      }
    }

    @SuppressWarnings("unchecked")
    public List<ValidationError> getErrors() {
        return (List<ValidationError>) Optional.ofNullable(getProperties())
                .map(it -> it.get(ERRORS_PROPERTY))
                .orElseGet(ArrayList::new);
    }
}
