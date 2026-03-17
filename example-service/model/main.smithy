$version: "2"

namespace com.example

use alloy#simpleRestJson

@simpleRestJson
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
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
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
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
        TransferLimitExceededDomainApiErrorException
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
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
        TransferLimitExceededDomainApiErrorException
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
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
        TransferLimitExceededDomainApiErrorException
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
