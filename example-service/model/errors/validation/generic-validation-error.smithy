$version: "2"

namespace com.example

// ------------ Generic Validation Error ------------
structure GenericValidationDetail with [ValidationErrorDetailMixin] {
    @const("error_code")
    @required
    code: String

    @memberExample("Some detail about the validation error")
    @required
    detail: String

    @memberExample("property")
    @required
    ref: String

    attributes: Document
}

list GenericValidationDetailList {
    member: GenericValidationDetail
}

@error("client")
@httpError(400)
structure GenericValidationError with [ValidationErrorMixin] {
    @const(400)
    @required
    status: Integer

    @memberExample("Validation failed")
    detail: String

    @memberExample("/api/v1/resource")
    instance: String

    @required
    errors: GenericValidationDetailList
}
