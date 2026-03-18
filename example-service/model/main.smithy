$version: "2"

namespace com.example

use alloy#discriminated
use alloy#simpleRestJson

@simpleRestJson
@title("Example API")
service ExampleService {
    version: "2024-01-01"
    operations: [
        ListProfiles
        GetProfile
        CreateProfile
        DeleteProfile
    ]
}

// List all profiles
@readonly
@http(method: "GET", uri: "/profiles")
operation ListProfiles {
    output := {
        @required
        profiles: ProfileList
    }

    errors: [
        // Validation errors
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
    ]
}

// Get a profile by ID
@readonly
@http(method: "GET", uri: "/profiles/{id}")
operation GetProfile {
    input := {
        @required
        @httpLabel
        id: String
    }

    output := {
        @required
        profile: ProfileUnion
    }

    errors: [
        // Validation errors
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
        TransferLimitExceededDomainApiErrorException
    ]
}

// Create a new profile
@http(method: "POST", uri: "/profile")
operation CreateProfile {
    input := {
        @required
        name: String

        @required
        email: String
    }

    output := {
        @required
        profile: ProfileUnion
    }

    errors: [
        // Validation errors
        ValidationApiErrorExceptionImpl
        // Domain errors
        AccountSuspendedDomainApiErrorException
        TransferLimitExceededDomainApiErrorException
    ]
}

// Delete a Profile
@idempotent
@http(method: "DELETE", uri: "/profiles/{id}")
operation DeleteProfile {
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

// Profile structure
@discriminated("type")
union ProfileUnion {
    PERSONAL: PersonalProfile
    BUSINESS: BusinessProfile
}

structure PersonalProfile {
    @required
    firstName: String

    @required
    lastName: String
}

structure BusinessProfile {
    @required
    companyName: String

    @required
    registrationNumber: String
}

list ProfileList {
    member: ProfileUnion
}
