$version: "2"

namespace com.example

// ------------ AccountSuspended Domain Error ------------
structure AccountSuspendedAttributes {
    @memberExample("Violation of terms of service")
    @required
    reason: String
}

@error("client")
@httpError(422)
structure AccountSuspendedDomainError with [DomainErrorMixin] {
    @const("Account Suspended")
    @required
    title: String

    @const(422)
    @required
    status: Integer

    @const("ACCOUNT_SUSPENDED")
    @required
    code: String

    @required
    attributes: AccountSuspendedAttributes
}
