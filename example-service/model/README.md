# Missing functionality
- Error validation against the actual schema
  - Since error are a custom trait we are missing proper validation.
  - Is there a way to get this out-of-the box?

# Custom functionality
- OpenAPI support for "const" instead of default
  - Default is not really a constant value...
  - Doesn't seem to be a way to support this out-of-the-box, so we need to implement our own schema generator.
- Error schemas using errorExample trait
  - Implementing own openAPI schema generator to support this.
  - Create /components/example to be reusable examples
  - Worth having it? Define per operation basis?