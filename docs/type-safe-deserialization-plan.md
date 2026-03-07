# Type-Safe Exception Deserialization Plan

## Context

When exceptions like `TransferLimitExceededException` are serialized to JSON and deserialized on the client side, type safety is lost. Currently:

```java
ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);
assertThat(deserialized.getProperties().get("code")).isEqualTo(original.getCode()); // Returns Object, not String
```

The goal is to enable type-safe deserialization where clients know exactly what fields each exception response contains at compile time.

## Approach: Deserialize Directly to Exception Classes

Reuse the existing exception classes for deserialization by adding `@JsonCreator` constructors. The `x-error-type` header determines which exception class to deserialize into.

### Key Design Decisions

1. **Reuse exception classes** - No separate response DTOs needed
2. **Use `x-error-type` header** - Maps directly to the exception class to deserialize
3. **Add `@JsonCreator` constructors** - Accept all JSON fields as parameters
4. **Add Jackson annotations to attributes** - Enable proper typed deserialization

## Implementation

### Files to Modify

| File | Change |
|------|--------|
| `TransferLimitExceededException.java` | Add `@JsonCreator` constructor |
| `AccountSuspendedException.java` | Add `@JsonCreator` constructor |
| `ValidationErrorResponseException.java` | Add `@JsonCreator` constructor |
| `AccessErrorResponseException.java` | Add `@JsonCreator` constructor |
| `ServerErrorResponseException.java` | Add `@JsonCreator` constructor |
| `TransferLimitExceededAttributes.java` | Add `@JsonCreator` annotation |
| `AccountSuspendedAttributes.java` | Add `@JsonCreator` annotation |

### Files to Create

| File | Description |
|------|-------------|
| `ApiErrorResponseDeserializer.java` | Maps `x-error-type` header to exception class |

### Exception Constructor Pattern

Each exception gets a new `@JsonCreator` constructor:

```java
@JsonCreator
public TransferLimitExceededException(
    @JsonProperty("type") URI type,
    @JsonProperty("title") String title,
    @JsonProperty("status") int status,
    @JsonProperty("detail") String detail,
    @JsonProperty("instance") String instance,
    @JsonProperty("code") String code,
    @JsonProperty("attributes") TransferLimitExceededAttributes attributes
) {
    super(buildProblemDetail(type, title, status, detail, instance, code, attributes), ERROR_TYPE);
}
```

### Deserializer Implementation

```java
public class ApiErrorResponseDeserializer {

    private static final Map<String, Class<? extends ApiErrorResponseException>> EXCEPTION_TYPES = Map.of(
        "TransferLimitExceededException", TransferLimitExceededException.class,
        "AccountSuspendedException", AccountSuspendedException.class,
        "ValidationErrorResponseException", ValidationErrorResponseException.class,
        "AccessErrorResponseException", AccessErrorResponseException.class,
        "ServerErrorResponseException", ServerErrorResponseException.class
    );

    private final ObjectMapper objectMapper;

    public <T extends ApiErrorResponseException> T deserialize(String json, String errorTypeHeader)
            throws JsonProcessingException {
        Class<? extends ApiErrorResponseException> targetClass = EXCEPTION_TYPES.get(errorTypeHeader);
        if (targetClass == null) {
            throw new IllegalArgumentException("Unknown error type: " + errorTypeHeader);
        }
        return (T) objectMapper.readValue(json, targetClass);
    }
}
```

### Implementation Order

1. Add `@JsonCreator` to attributes records
2. Add `@JsonCreator` constructor to each exception class
3. Create `ApiErrorResponseDeserializer` utility
4. Add tests

## Usage After Implementation

```java
ApiErrorResponseDeserializer deserializer = new ApiErrorResponseDeserializer(objectMapper);

// Get header from HTTP response
String errorType = response.getHeaders().getFirst("x-error-type");
String body = response.getBody();

// Deserialize directly to exception
TransferLimitExceededException exception = deserializer.deserialize(body, errorType);

// Full type safety - same API as when throwing the exception!
BigDecimal amount = exception.getAttributes().amount();  // Compile-time typed
String currency = exception.getAttributes().currency();  // Compile-time typed
String code = exception.getCode();                       // Compile-time typed
```

## Key Files Reference

- `TransferLimitExceededException.java` - Add `@JsonCreator` constructor
- `TransferLimitExceededAttributes.java` - Add `@JsonCreator` annotation
- `ApiErrorResponseException.java` - Contains `ERROR_TYPE_HEADER` constant

## Verification

1. Run existing tests to ensure no regression
2. Add round-trip tests that:
   - Serialize exception to JSON
   - Deserialize JSON back to same exception type
   - Verify all typed getters return correct values
