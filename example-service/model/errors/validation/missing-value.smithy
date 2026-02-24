$version: "2"

namespace com.example

// ------------ MissingValue Validation Error ------------
structure MissingValueValidationDetail with [ValidationErrorDetailMixin] {
    @const("missing_value")
    @required
    code: String
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
    @const(400)
    @required
    status: Integer

    @required
    errors: MissingValueValidationDetailList
}
