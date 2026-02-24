package com.example.exception.domain;

import com.example.exception.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Optional;

/**
 * Problem detail for domain-specific business errors.
 * Sealed to only permit specific domain problem detail implementations.
 */
public abstract sealed class DomainProblemDetail extends ProblemDetail
        permits TransferLimitExceededProblemDetail, AccountSuspendedProblemDetail {

    private static final URI TYPE = URI.create("https://errors.example.com/domain-error");
    private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
    private static final String CODE_PROPERTY = "code";
    private static final String ATTRIBUTES_PROPERTY = "attributes";

    protected DomainProblemDetail(String code, String title, String detail, ErrorAttributes attributes) {
        this(DEFAULT_STATUS, code, title, detail, attributes);
    }

    protected DomainProblemDetail(HttpStatus status, String code, String title, String detail, ErrorAttributes attributes) {
        super(status.value());
        setType(TYPE);
        setTitle(title);
        if (detail != null) {
            setDetail(detail);
        }
        setProperty(CODE_PROPERTY, code);
        setProperty(ATTRIBUTES_PROPERTY, attributes);
    }

    public String getCode() {
        return (String) Optional.ofNullable(getProperties())
                .map(it -> it.get(CODE_PROPERTY))
                .orElse(null);
    }

    public ErrorAttributes getAttributes() {
        return (ErrorAttributes) Optional.ofNullable(getProperties())
                .map(it -> it.get(ATTRIBUTES_PROPERTY))
                .orElse(null);
    }
}
