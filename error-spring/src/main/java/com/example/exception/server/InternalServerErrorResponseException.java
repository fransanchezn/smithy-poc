package com.example.exception.server;

/**
 * Exception for internal server-side errors that are not exposed to API consumers. These errors are
 * for internal use only and should be handled/transformed before reaching the API boundary.
 */
public non-sealed class InternalServerErrorResponseException extends ServerErrorResponseException {

  private InternalServerErrorResponseException(ServerProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ServerErrorResponseException.Builder<InternalServerErrorResponseException> {

    private Builder() {
    }

    @Override
    public InternalServerErrorResponseException build() {
      return new InternalServerErrorResponseException(problemDetail, cause);
    }
  }
}
