$version: "2"

namespace com.example

// --------- Validation Error ---------
@trait
structure validationError {}

/// Validation error following RFC 7807 with errors array
@mixin
@validationError
structure ValidationApiErrorException with [ApiErrorException] {
    @const("/errors/types/validation")
    @required
    type: String

    @const("Validation Problem")
    @required
    title: String

    @const(400)
    @required
    status: Integer

    @memberExample("Validation failed")
    detail: String

    @memberExample("/api/v1/users")
    instance: String

    @required
    errors: ValidationErrorList
}

/// Base mixin for a single validation error detail
@mixin
structure ValidationErrorMixin {
    @required
    @memberExample("Validation error detail")
    detail: String

    @required
    @const("validation_error_code")
    code: String

    @required
    @memberExample("field")
    ref: String
}

list ValidationErrorList {
    member: ValidationErrorUnion
}

union ValidationErrorUnion {
    missingValueValidationError: MissingValueValidationError
    invalidFormatValidationError: InvalidFormatValidationError
}

@error("client")
@httpError(400)
structure ValidationApiErrorExceptionImpl with [ValidationApiErrorException] {
    @memberExample([
        {
            missingValueValidationError: { code: "missing_value", detail: "Name is required", ref: "name" }
        }
        {
            invalidFormatValidationError: {
                code: "invalid_format"
                detail: "Email must be a valid email address"
                ref: "email"
                attributes: { pattern: "^[a-zA-Z0-9]+$" }
            }
        }
    ])
    @required
    errors: ValidationErrorList
}
