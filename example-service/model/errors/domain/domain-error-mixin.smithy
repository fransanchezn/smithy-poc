$version: "2"

namespace com.example

// --------- Domain Error ---------
@trait
structure domainError {}

@mixin
@domainError
structure DomainProblemDetailMixin with [ProblemDetailMixin] {
    @const("https://errors.example.com/domain-error")
    @required
    type: String

    @required
    code: String
}
