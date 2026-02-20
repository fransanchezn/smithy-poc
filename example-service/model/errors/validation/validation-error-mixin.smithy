$version: "2"

namespace com.example

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
    @const
    @required
    type: String = "/errors/types/validation"

    @const
    @required
    title: String = "Validation Problem"
}
