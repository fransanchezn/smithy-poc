package com.example.exception.server;

/**
 * Exception for public server-side errors that are exposed to API consumers.
 */
public non-sealed class PublicServerErrorResponseException extends ServerErrorResponseException {

  private PublicServerErrorResponseException(ServerProblemDetail problemDetail, Throwable cause) {
    super(problemDetail, cause);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends ServerErrorResponseException.Builder<PublicServerErrorResponseException> {

    private Builder() {
    }

    @Override
    public PublicServerErrorResponseException build() {
      return new PublicServerErrorResponseException(problemDetail, cause);
    }
  }
}
