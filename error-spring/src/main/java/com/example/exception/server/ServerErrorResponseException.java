package com.example.exception.server;

import com.example.exception.ApiErrorResponseException;

/**
 * Exception for server-side errors (internal errors, service unavailable).
 */
public final class ServerErrorResponseException extends ApiErrorResponseException {

  private ServerErrorResponseException(ServerProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ApiErrorResponseException.Builder<ServerProblemDetail, ServerErrorResponseException> {

    private Builder() {
    }

    @Override
    public ServerErrorResponseException build() {
      return new ServerErrorResponseException(problemDetail, cause);
    }
  }
}
