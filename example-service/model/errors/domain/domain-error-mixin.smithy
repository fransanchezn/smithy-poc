$version: "2"

namespace com.example

// --------- Domain Error ---------
@trait
structure domainError {}

@mixin
@domainError
structure DomainProblemDetailMixin with [ProblemDetailMixin] {
    @const("/errors/types/domain")
    @required
    type: String

    @required
    code: String
}
