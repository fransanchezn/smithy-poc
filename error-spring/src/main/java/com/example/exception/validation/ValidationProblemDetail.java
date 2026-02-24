package com.example.exception.validation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Problem detail for validation errors.
 * Sealed to only permit specific validation problem detail implementations.
 * Detail is not supported at root level - each ValidationError has its own detail.
 */
public abstract sealed class ValidationProblemDetail extends ProblemDetail
        permits InvalidFormatProblemDetail, MissingValueProblemDetail {

    private static final URI TYPE = URI.create("/errors/types/validation");
    private static final String TITLE = "Validation Problem";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String ERRORS_PROPERTY = "errors";

    protected ValidationProblemDetail() {
        super(STATUS.value());
        setType(TYPE);
        setTitle(TITLE);
        setProperty(ERRORS_PROPERTY, new ArrayList<ValidationError>());
    }

    protected ValidationProblemDetail(List<? extends ValidationError> errors) {
        super(STATUS.value());
        setType(TYPE);
        setTitle(TITLE);
        setProperty(ERRORS_PROPERTY, new ArrayList<>(errors));
    }

    public ValidationProblemDetail addError(ValidationError error) {
        getErrors().add(error);
        return this;
    }

    @SuppressWarnings("unchecked")
    public List<ValidationError> getErrors() {
        return (List<ValidationError>) Optional.ofNullable(getProperties())
                .map(it -> it.get(ERRORS_PROPERTY))
                .orElseGet(ArrayList::new);
    }
}
