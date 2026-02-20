$version: "2"

namespace com.example

/// Indicates this member has a constant value that cannot change.
/// When applied to a member with a default value, generates OpenAPI "const" instead of "default".
@trait(selector: "structure > member :test(> simpleType)")
structure const {}
