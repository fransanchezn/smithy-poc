package com.example.exception.server;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/**
 * Problem detail for server-side errors (internal errors, service unavailable).
 */
public final class ServerProblemDetail extends ProblemDetail {

    private static final URI TYPE = URI.create("https://errors.example.com/server-error");

    private ServerProblemDetail(HttpStatus status, String title, String detail) {
        super(status.value());
        setType(TYPE);
        setTitle(title);
        if (detail != null) {
            setDetail(detail);
        }
    }

    public static ServerProblemDetail of(HttpStatus status, String title, String detail) {
        return new ServerProblemDetail(status, title, detail);
    }

    public static ServerProblemDetail internalServerError(String detail) {
        return new ServerProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", detail);
    }

    public static ServerProblemDetail serviceUnavailable(String detail) {
        return new ServerProblemDetail(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", detail);
    }
}
