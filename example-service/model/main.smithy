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
        // Validation errors
        ValidationProblemDetail
        // Domain errors
        AccountSuspendedDomainProblemDetail
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
        // Validation errors
        ValidationProblemDetail
        // Domain errors
        AccountSuspendedDomainProblemDetail
        TransferLimitExceededDomainProblemDetail
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
        // Validation errors
        ValidationProblemDetail
        // Domain errors
        AccountSuspendedDomainProblemDetail
        TransferLimitExceededDomainProblemDetail
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
        // Validation errors
        ValidationProblemDetail
        // Domain errors
        AccountSuspendedDomainProblemDetail
        TransferLimitExceededDomainProblemDetail
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
