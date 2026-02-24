package com.example.exception.domain;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Problem detail for account suspended errors.
 */
public final class AccountSuspendedProblemDetail extends DomainProblemDetail {

    private static final String CODE = "ACCOUNT_SUSPENDED";
    private static final String TITLE = "Account Suspended";

    public AccountSuspendedProblemDetail() {
        super(CODE, TITLE, null, null);
    }

    public AccountSuspendedProblemDetail(String detail, AccountSuspendedAttributes attributes) {
        super(CODE, TITLE, detail, attributes);
    }

    public AccountSuspendedProblemDetail(String detail, String reason) {
        this(detail, new AccountSuspendedAttributes(reason));
    }

    @JsonSetter(ATTRIBUTES_PROPERTY)
    private void setAttributes(AccountSuspendedAttributes attributes) {
        setProperty(ATTRIBUTES_PROPERTY, attributes);
    }

    @Override
    public AccountSuspendedAttributes getAttributes() {
        return (AccountSuspendedAttributes) super.getAttributes();
    }
}
