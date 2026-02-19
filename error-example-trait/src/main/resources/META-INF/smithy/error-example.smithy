$version: "2"

namespace com.example

/// Defines an example for an error structure that will be automatically
/// included in OpenAPI components/examples and referenced in error responses.
@trait(selector: "structure[trait|error]")
list errorExample {
    member: ErrorExampleEntry
}

/// A single error example entry
structure ErrorExampleEntry {
    /// A short title for the example
    @required
    title: String

    /// Optional description of when this error occurs
    documentation: String

    /// The example content (must match the error structure)
    @required
    content: Document
}
