package com.example.exception.server;

import com.example.exception.ApiErrorResponseException;

/**
 * Exception for server-side errors (internal errors, service unavailable). Sealed to only permit
 * public and internal server exception categories.
 */
public abstract sealed class ServerErrorResponseException extends ApiErrorResponseException
    permits PublicServerErrorResponseException, InternalServerErrorResponseException {

  protected ServerErrorResponseException(ServerProblemDetail problemDetail) {
    super(problemDetail);
  }

  protected ServerErrorResponseException(ServerProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public ServerProblemDetail getProblemDetail() {
    return (ServerProblemDetail) getBody();
  }

  public abstract static class Builder<T extends ServerErrorResponseException>
      extends ApiErrorResponseException.Builder<ServerProblemDetail, T> {

    protected Builder() {
    }
  }
}
