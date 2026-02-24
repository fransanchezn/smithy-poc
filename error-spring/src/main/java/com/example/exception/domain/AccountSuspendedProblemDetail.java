package com.example.exception.domain;

/**
 * Problem detail for account suspended errors.
 */
public final class AccountSuspendedProblemDetail extends DomainProblemDetail {

    private static final String CODE = "ACCOUNT_SUSPENDED";
    private static final String TITLE = "Account Suspended";

    public AccountSuspendedProblemDetail(String detail, AccountSuspendedAttributes attributes) {
        super(CODE, TITLE, detail, attributes);
    }

    public AccountSuspendedProblemDetail(String detail, String reason) {
        this(detail, new AccountSuspendedAttributes(reason));
    }

    @Override
    public AccountSuspendedAttributes getAttributes() {
        return (AccountSuspendedAttributes) super.getAttributes();
    }
}
