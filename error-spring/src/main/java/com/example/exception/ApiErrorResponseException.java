package com.example.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Base exception for all API errors that follow RFC 7807 Problem Details.
 * Extends Spring Boot's ErrorResponseException to integrate with Spring's error handling.
 */
public abstract class ApiErrorResponseException extends ErrorResponseException {

    protected ApiErrorResponseException(ProblemDetail problemDetail) {
        super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, null);
    }

    protected ApiErrorResponseException(ProblemDetail problemDetail, Throwable cause) {
        super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, cause);
    }
}
