$version: "2"

namespace com.example

// --------- Base API Error Exception ---------
@mixin
structure ApiErrorException {
    @required
    type: String

    @required
    title: String

    @required
    status: Integer

    detail: String

    instance: String
}
