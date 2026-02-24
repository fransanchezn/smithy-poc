package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.math.BigDecimal;

/**
 * Problem detail for transfer limit exceeded errors.
 */
public final class TransferLimitExceededProblemDetail extends DomainProblemDetail {

    private static final String CODE = "TRANSFER_LIMIT_EXCEEDED";
    private static final String TITLE = "Transfer Limit Exceeded";

    public TransferLimitExceededProblemDetail() {
        super(CODE, TITLE, null, null);
    }

    public TransferLimitExceededProblemDetail(String detail, TransferLimitExceededAttributes attributes) {
        super(CODE, TITLE, detail, attributes);
    }

    public TransferLimitExceededProblemDetail(String detail, BigDecimal amount, String currency) {
        this(detail, new TransferLimitExceededAttributes(amount, currency));
    }

    @JsonSetter(ATTRIBUTES_PROPERTY)
    private void setAttributes(TransferLimitExceededAttributes attributes) {
        setProperty(ATTRIBUTES_PROPERTY, attributes);
    }

    @Override
    public TransferLimitExceededAttributes getAttributes() {
        return (TransferLimitExceededAttributes) super.getAttributes();
    }
}
