$version: "2"

namespace com.example

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

    errors: [
        AccountSuspendedDomainError
        MissingValueValidationError
    ]
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
        MissingValueValidationError
        InvalidFormatValidationError
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

    errors: [
        AccountSuspendedDomainError
        TransferLimitExceededDomainError
        MissingValueValidationError
        InvalidFormatValidationError
    ]
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
        MissingValueValidationError
        InvalidFormatValidationError
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
@errorExample([
    {
        title: "Transfer limit exceeded error"
        documentation: "Returned when a transfer exceeds the allowed limit"
        content: {
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
@errorExample([
    {
        title: "Account suspended error"
        documentation: "Returned when the user's account has been suspended"
        content: {
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
@trait
structure validationError {}

/// Base mixin for a single validation error detail
@mixin
structure ValidationErrorDetailMixin {
    @required
    detail: String

    @required
    code: String

    @required
    ref: String
}

/// Validation error following RFC 7807 with errors array
@mixin
@validationError
structure ValidationErrorMixin with [ProblemDetailMixin] {
    @required
    type: String = "/errors/types/validation"

    @required
    title: String = "Validation Problem"
}

// ------------ MissingValue Validation Error ------------
structure MissingValueValidationDetail with [ValidationErrorDetailMixin] {
    @required
    code: String = "missing_value"
}

list MissingValueValidationDetailList {
    member: MissingValueValidationDetail
}

@errorExample([
    {
        title: "Missing value validation error"
        documentation: "Returned when required fields are missing"
        content: {
            type: "/errors/types/validation"
            title: "Validation Problem"
            status: 400
            detail: "Validation failed"
            instance: "/api/v1/users"
            errors: [
                {
                    detail: "Name is required"
                    code: "missing_value"
                    ref: "name"
                }
            ]
        }
    }
])
@error("client")
@httpError(400)
structure MissingValueValidationError with [ValidationErrorMixin] {
    @required
    errors: MissingValueValidationDetailList
}

// ------------ InvalidFormat Validation Error ------------
structure InvalidFormatAttributes {
    @required
    pattern: String
}

structure InvalidFormatValidationDetail with [ValidationErrorDetailMixin] {
    @required
    code: String = "invalid_format"

    attributes: InvalidFormatAttributes
}

list InvalidFormatValidationDetailList {
    member: InvalidFormatValidationDetail
}

@errorExample([
    {
        title: "Invalid format validation error"
        documentation: "Returned when field values don't match expected format"
        content: {
            type: "/errors/types/validation"
            title: "Validation Problem"
            status: 400
            detail: "Validation failed"
            instance: "/api/v1/users"
            errors: [
                {
                    detail: "Email must be a valid email address"
                    code: "invalid_value"
                    ref: "email"
                    attributes: { pattern: "^[a-zA-Z0-9]+$" }
                }
            ]
        }
    }
])
@error("client")
@httpError(400)
structure InvalidFormatValidationError with [ValidationErrorMixin] {
    @required
    errors: InvalidFormatValidationDetailList
}
