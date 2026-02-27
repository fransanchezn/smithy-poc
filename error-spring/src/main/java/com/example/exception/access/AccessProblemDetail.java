package com.example.exception.access;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Problem detail for access-related errors (authentication, authorization).
 */
public final class AccessProblemDetail extends ProblemDetail {

  private static final URI TYPE = URI.create("/errors/types/access");
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNAUTHORIZED;

  public AccessProblemDetail() {
    super();
  }

  AccessProblemDetail(HttpStatus status, String title, String detail) {
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

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private HttpStatus status = DEFAULT_STATUS;
    private String title;
    private String detail;

    private Builder() {
    }

    public Builder status(HttpStatus status) {
      this.status = status;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public AccessProblemDetail build() {
      return new AccessProblemDetail(status, title, detail);
    }
  }
}
