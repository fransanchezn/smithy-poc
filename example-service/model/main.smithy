$version: "2"

namespace com.example

use alloy#dataExamples
use aws.protocols#restJson1

@restJson1
@title("Example API")
service ExampleService {
    version: "2024-01-01"
    operations: [
        ListUsers
        GetUser
        CreateUser
        DeleteUser
    ]
}

// List all users
@readonly
@http(method: "GET", uri: "/users")
operation ListUsers {
    output := {
        @required
        users: UserList
    }
}

// Get a user by ID
@readonly
@http(method: "GET", uri: "/users/{id}")
operation GetUser {
    input := {
        @required
        @httpLabel
        id: String
    }

    output := {
        @required
        user: User
    }

    errors: [
        AccountSuspendedDomainError
        TransferLimitExceededDomainError
    ]
}

// Create a new user
@http(method: "POST", uri: "/users")
operation CreateUser {
    input := {
        @required
        name: String

        @required
        email: String
    }

    output := {
        @required
        user: User
    }
}

// Delete a user
@idempotent
@http(method: "DELETE", uri: "/users/{id}")
operation DeleteUser {
    input := {
        @required
        @httpLabel
        id: String
    }

    errors: [
        AccountSuspendedDomainError
        TransferLimitExceededDomainError
    ]
}

// User structure
structure User {
    @required
    id: String

    @required
    name: String

    @required
    email: String

    createdAt: Timestamp
}

list UserList {
    member: User
}

// --------- Problem Detail (base class) ---------
@mixin
structure ProblemDetailMixin {
    @required
    type: String

    @required
    title: String

    detail: String

    instance: String
}

// --------- Domain Error ---------
@trait
structure domainError {}

@mixin
@domainError
structure DomainErrorMixin with [ProblemDetailMixin] {
    @required
    type: String = "https://errors.example.com/domain-error"

    @required
    title: String

    @required
    errorCode: String
}

// ------------ TransferLimitExceeded Domain Error ------------
@dataExamples([
    {
        smithy: {
            type: "https://errors.example.com/domain-error"
            title: "Transfer Limit Exceeded"
            errorCode: "TRANSFER_LIMIT_EXCEEDED"
            attributes: { amount: 15000.00, currency: "USD" }
        }
    }
])
@error("client")
@httpError(422)
structure TransferLimitExceededDomainError with [DomainErrorMixin] {
    @required
    title: String = "Transfer Limit Exceeded"

    @required
    errorCode: String = "TRANSFER_LIMIT_EXCEEDED"

    @required
    attributes: TransferLimitAttributes
}

structure TransferLimitAttributes {
    @required
    amount: Double

    @required
    currency: String
}

// ------------ AccountSuspended Domain Error ------------
@dataExamples([
    {
        smithy: {
            type: "https://errors.example.com/domain-error"
            title: "Account Suspended"
            errorCode: "ACCOUNT_SUSPENDED"
            attributes: { reason: "Violation of terms of service" }
        }
    }
])
@error("client")
@httpError(422)
structure AccountSuspendedDomainError with [DomainErrorMixin] {
    @required
    title: String = "Account Suspended"

    @required
    errorCode: String = "ACCOUNT_SUSPENDED"

    @required
    attributes: AccountSuspendedAttributes
}

structure AccountSuspendedAttributes {
    @required
    reason: String
}
// --------- Validation Error ---------
// --------- Endpoint Errors ---------
