$version: "2"

namespace com.example

/// Indicates this member has a constant value that cannot change.
/// The value provided will be used as the OpenAPI "const" value.
@trait(selector: "structure > member :test(> simpleType)")
document const
