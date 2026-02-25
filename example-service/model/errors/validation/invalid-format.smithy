$version: "2"

namespace com.example

// ------------ InvalidFormat Validation Error ------------
structure InvalidFormatAttributes {
    @memberExample("^[a-zA-Z0-9]+$")
    @required
    pattern: String
}

structure InvalidFormatValidationDetail with [ValidationErrorDetailMixin] {
    @const("invalid_format")
    @required
    code: String

    @memberExample("Email must be a valid email address")
    @required
    detail: String

    @memberExample("email")
    @required
    ref: String

    attributes: InvalidFormatAttributes
}
