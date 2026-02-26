package com.example.exception.access;

import com.example.exception.ApiErrorResponseException;
import org.springframework.http.HttpStatus;

/**
 * Exception for access-related errors (authentication, authorization).
 */
public final class AccessErrorResponseException extends ApiErrorResponseException {

    public AccessErrorResponseException(AccessProblemDetail problemDetail) {
        super(problemDetail);
    }

    public AccessErrorResponseException(AccessProblemDetail problemDetail, Throwable cause) {
        super(problemDetail, cause);
    }

    public static AccessErrorResponseException unauthorized(String detail) {
        return new AccessErrorResponseException(AccessProblemDetail.unauthorized(detail));
    }

    public static AccessErrorResponseException forbidden(String detail) {
        return new AccessErrorResponseException(AccessProblemDetail.forbidden(detail));
    }
}
