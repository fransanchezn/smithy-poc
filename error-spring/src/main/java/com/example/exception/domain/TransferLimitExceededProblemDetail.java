package com.example.exception.domain;

import java.math.BigDecimal;

/**
 * Problem detail for transfer limit exceeded errors.
 */
public final class TransferLimitExceededProblemDetail extends DomainProblemDetail {

    private static final String CODE = "TRANSFER_LIMIT_EXCEEDED";
    private static final String TITLE = "Transfer Limit Exceeded";

    public TransferLimitExceededProblemDetail(String detail, TransferLimitExceededAttributes attributes) {
        super(CODE, TITLE, detail, attributes);
    }

    public TransferLimitExceededProblemDetail(String detail, BigDecimal amount, String currency) {
        this(detail, new TransferLimitExceededAttributes(amount, currency));
    }

    @Override
    public TransferLimitExceededAttributes getAttributes() {
        return (TransferLimitExceededAttributes) super.getAttributes();
    }
}
