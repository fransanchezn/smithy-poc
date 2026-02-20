$version: "2"

namespace com.example

// ------------ AccountSuspended Domain Error ------------
@errorExample([
    {
        title: "Account suspended error"
        documentation: "Returned when the user's account has been suspended"
        content: {
            type: "https://errors.example.com/domain-error"
            title: "Account Suspended"
            status: 422
            code: "ACCOUNT_SUSPENDED"
            attributes: { reason: "Violation of terms of service" }
        }
    }
])
@error("client")
@httpError(422)
structure AccountSuspendedDomainError with [DomainErrorMixin] {
    @const
    @required
    title: String = "Account Suspended"

    @const
    @required
    status: Integer = 422

    @const
    @required
    code: String = "ACCOUNT_SUSPENDED"

    @required
    attributes: AccountSuspendedAttributes
}

structure AccountSuspendedAttributes {
    @required
    reason: String
}
