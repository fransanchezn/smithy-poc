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
