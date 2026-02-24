package com.example.exception.server;

import com.example.exception.ApiErrorResponseException;
import org.springframework.http.HttpStatus;

/**
 * Exception for server-side errors (internal errors, service unavailable).
 */
public final class ServerErrorResponseException extends ApiErrorResponseException {

    public ServerErrorResponseException(ServerProblemDetail problemDetail) {
        super(problemDetail);
    }

    public ServerErrorResponseException(ServerProblemDetail problemDetail, Throwable cause) {
        super(problemDetail, cause);
    }

    public static ServerErrorResponseException internalServerError(String detail) {
        return new ServerErrorResponseException(ServerProblemDetail.internalServerError(detail));
    }

    public static ServerErrorResponseException internalServerError(String detail, Throwable cause) {
        return new ServerErrorResponseException(ServerProblemDetail.internalServerError(detail), cause);
    }

    public static ServerErrorResponseException serviceUnavailable(String detail) {
        return new ServerErrorResponseException(ServerProblemDetail.serviceUnavailable(detail));
    }

    public static ServerErrorResponseException of(HttpStatus status, String title, String detail) {
        return new ServerErrorResponseException(ServerProblemDetail.of(status, title, detail));
    }
}
