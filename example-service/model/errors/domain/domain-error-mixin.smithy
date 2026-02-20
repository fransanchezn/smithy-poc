$version: "2"

namespace com.example

// --------- Domain Error ---------
@trait
structure domainError {}

@mixin
@domainError
structure DomainErrorMixin with [ProblemDetailMixin] {
    @const
    @required
    type: String = "https://errors.example.com/domain-error"

    @required
    code: String
}
