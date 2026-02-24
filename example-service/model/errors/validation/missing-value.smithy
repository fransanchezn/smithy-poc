$version: "2"

namespace com.example

// ------------ MissingValue Validation Error ------------
structure MissingValueValidationDetail with [ValidationErrorDetailMixin] {
    @const("missing_value")
    @required
    code: String

    @memberExample("Name is required")
    @required
    detail: String

    @memberExample("name")
    @required
    ref: String
}

list MissingValueValidationDetailList {
    member: MissingValueValidationDetail
}

@error("client")
@httpError(400)
structure MissingValueValidationError with [ValidationErrorMixin] {
    @const(400)
    @required
    status: Integer

    @memberExample("Validation failed")
    detail: String

    @memberExample("/api/v1/users")
    instance: String

    @required
    errors: MissingValueValidationDetailList
}
