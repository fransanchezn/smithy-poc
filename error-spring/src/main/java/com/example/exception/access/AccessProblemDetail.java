package com.example.exception.access;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * Problem detail for access-related errors (authentication, authorization).
 */
public final class AccessProblemDetail extends ProblemDetail {

    private static final URI TYPE = URI.create("/errors/types/access");

    public AccessProblemDetail() {
        super();
    }

    private AccessProblemDetail(HttpStatus status, String title, String detail) {
        super(status.value());
        setType(TYPE);
        setTitle(title);
        if (detail != null) {
            setDetail(detail);
        }
    }

    public static AccessProblemDetail of(HttpStatus status, String title, String detail) {
        return new AccessProblemDetail(status, title, detail);
    }

    public static AccessProblemDetail unauthorized(String detail) {
        return new AccessProblemDetail(HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
    }

    public static AccessProblemDetail forbidden(String detail) {
        return new AccessProblemDetail(HttpStatus.FORBIDDEN, "Forbidden", detail);
    }
}
