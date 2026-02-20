$version: "2"

namespace com.example

// --------- Problem Detail (base class) ---------
@mixin
structure ProblemDetailMixin {
    @required
    type: String

    @required
    title: String

    @required
    status: Integer

    detail: String

    instance: String
}
