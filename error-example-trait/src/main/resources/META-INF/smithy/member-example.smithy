$version: "2"

namespace com.example

/// Defines an example value for a structure member.
/// Used to generate OpenAPI examples at the component level.
/// If the member also has @const, the const value takes precedence.
@trait(selector: "structure > member")
document memberExample
