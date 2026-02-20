$version: "2"

namespace com.example

// ------------ MissingValue Validation Error ------------
structure MissingValueValidationDetail with [ValidationErrorDetailMixin] {
    @const
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
    @const
    @required
    status: Integer = 400

    @required
    errors: MissingValueValidationDetailList
}
