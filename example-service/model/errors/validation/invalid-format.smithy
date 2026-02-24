$version: "2"

namespace com.example

// ------------ InvalidFormat Validation Error ------------
structure InvalidFormatAttributes {
    @required
    pattern: String
}

structure InvalidFormatValidationDetail with [ValidationErrorDetailMixin] {
    @const("invalid_format")
    @required
    code: String

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
    @const(400)
    @required
    status: Integer

    @required
    errors: InvalidFormatValidationDetailList
}
