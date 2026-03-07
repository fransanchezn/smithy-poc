# Smithy API errors

## Context
We want to define how our API errors are defined in Smithy. Errors in Smithy are defined as follows:
```smithy
@error("client")
@httpError(429)
structure ThrottlingError {
    @required
    message: String
}
```

Then in the operation definition, we can associate errors to given operations:
```smithy
// List all users
@readonly
@http(method: "GET", uri: "/users")
operation ListUsers {
    output := {
        @required
        users: UserList
    }

    errors: [
        ThrottlingError
    ]
}
```

This will allow us to link errors and API endpoints to provide great OpenAPI Documentation as well as generate service/client side code to handle these errors gracefully.

## Smithy structure
Our custom implementation for Wise follows this structure. We have different types of errors Access, Service, Domain and Validation errors. These can be extended with more specific errors.

- Access error: Unauthorized, unauthenticated, rate limited...
- Service errors: Oops, something went wrong (500)
- Validation errors: When we validate payloads and we are missing information and is not correctly formatted.
- Domain errors: Business logic validations and checks such as an account being suspended.

For more information about our errors visit repository: https://github.com/transferwise/wise-api-contracts/tree/main/wise-api-contracts-errors

We are implementing the RFC-9457: https://www.rfc-editor.org/rfc/rfc9457.html to structure our errors. The basic problem detail structure is:
### Custom traits
We need few custom traits mainly to improve our OpenAPI documentation. This will probably help us as well to generate the Java classes to support inheritance and constant values.

#### Error type const
These traits are accessError, serviceError, validationError and domainError. They will be defined:
```smithy
// --------- Access Error ---------
@trait
structure accessError {}

// --------- Service Error ---------
@trait
structure serviceError {}

// --------- Validation Error ---------
@trait
structure validationError {}

// --------- Domain Error ---------
@trait
structure domainError {}
```

These traits are going to help us determine to which "category" each specific error belongs.

#### Const (constant values)
Trait that is applicable to members of a structure to indicate it is a constant value. It will be transformed into a static value in Java. This is NOT a default but a specific immutable value
```smithy
@const("/errors/types/domain")
@required
type: String
```

#### MemberExamples (examples)
This trait help us generate better openAPI specs where we can provide example for given fields. This won't have an impact in the java generated code (maybe javadoc if we want to generate examples).
```smithy
@memberExample("/api/v1/users")
instance: String
```
### Error structure
The error structure follows a flat hierarchy with a single level of inheritance from `ApiErrorResponseException`:

```mermaid
classDiagram
    class ErrorResponseException {
        <<Spring Framework>>
    }

    class ApiErrorResponseException {
        <<abstract>>
        +getType(): URI
        +getTitle(): String
        +getStatus(): int
        +getDetail(): String
        +getInstance(): URI
    }

    class TransferLimitExceededException {
        <<final>>
        +getCode(): TransferErrorCode
        +getAttributes(): TransferLimitExceededAttributes
    }

    class AccountSuspendedException {
        <<final>>
        +getCode(): AccountErrorCode
        +getAttributes(): AccountSuspendedAttributes
    }

    class ValidationErrorResponseException {
        <<final>>
        +getErrors(): List~ValidationError~
    }

    class InvalidTokenAccessErrorResponseException {
        <<final>>
        +getCode(): AccessErrorCode
    }

    class InternalServerErrorResponseException {
        <<final>>
    }

    ErrorResponseException <|-- ApiErrorResponseException
    ApiErrorResponseException <|-- TransferLimitExceededException
    ApiErrorResponseException <|-- AccountSuspendedException
    ApiErrorResponseException <|-- ValidationErrorResponseException
    ApiErrorResponseException <|-- InvalidTokenAccessErrorResponseException
    ApiErrorResponseException <|-- InternalServerErrorResponseException

    TransferLimitExceededException *-- TransferLimitExceededAttributes
    AccountSuspendedException *-- AccountSuspendedAttributes

    ValidationErrorResponseException *-- "0..*" ValidationError

    ValidationError <|-- InvalidFormatValidationError
    ValidationError <|-- MissingValueValidationError

    InvalidFormatValidationError *-- InvalidFormatAttributes
    MissingValueValidationError *-- MissingValueAttributes

    class ErrorAttributes {
        <<interface>>
    }

    ErrorAttributes <|.. TransferLimitExceededAttributes
    ErrorAttributes <|.. AccountSuspendedAttributes
    ErrorAttributes <|.. InvalidFormatAttributes
    ErrorAttributes <|.. MissingValueAttributes
```


```smithy
// --------- Base API Error Exception ---------
@mixin
structure ApiErrorException {
    @required
    type: String

    @required
    title: String

    @required
    status: Integer

    detail: String

    instance: String
}
```
### Access errors structure
Access errors follow a similar pattern to domain errors with a typed error code:

```smithy
// --------- Access Error ---------
@trait
structure accessError {}

@mixin
@accessError
structure AccessApiErrorException with [ApiErrorException] {
    @const("/errors/types/access")
    @required
    type: String

    @required
    code: String
}

@error("client")
@httpError(401)
structure InvalidTokenAccessApiErrorException with [AccessApiErrorException] {
    @const("Access Error")
    @required
    title: String

    @const(401)
    @required
    status: Integer

    @const("Invalid Token")
    @required
    detail: String

    @const("unauthorized")
    @required
    code: String
}
```

### Server errors structure
[TBD] - Should follow a very similar structure to domain errors

### Domain errors structure
The domain errors schema:

```smithy
// --------- Domain Error ---------
@trait
structure domainError {}

@mixin
@domainError
structure DomainApiErrorException with [ApiErrorException] {
    @const("/errors/types/domain")
    @required
    type: String

    @required
    code: String
}
```

Specific example:
```smithy
// ------------ TransferLimitExceeded Domain Error ------------
structure TransferLimitExceededAttributes {
    @memberExample(15000.00)
    @required
    amount: BigDecimal

    @memberExample("USD")
    @required
    currency: String
}

@error("client")
@httpError(422)
structure TransferLimitExceededDomainApiErrorException with [DomainApiErrorException] {
    @const("Transfer Limit Exceeded")
    @required
    title: String

    @const(422)
    @required
    status: Integer

    @const("Transfer Limit has been exceeded")
    @required
    detail: String

    @const("transfer.transfer_limit_exceeded")
    @required
    code: String

    @required
    attributes: TransferLimitExceededAttributes
}
```

### Validation errors structure
Validation errors are slightly different because we can return a list of errors. We are not documenting every single error in every endpoint since it could be very noisy so we are having a generic example with all the possible schemas on every endpoint (ValidationProblemDetail). Then we will have all the possible ValidationDetail implementations defined

```smithy
// --------- Validation Error ---------
@trait
structure validationError {}

@mixin
@validationError
structure ValidationApiErrorException with [ApiErrorException] {
    @const("/errors/types/validation")
    @required
    type: String

    @const("Validation Problem")
    @required
    title: String

    @const(400)
    @required
    status: Integer

    @memberExample("Validation failed")
    detail: String

    @memberExample("/api/v1/users")
    instance: String

    @required
    errors: ValidationErrorList
}

@mixin
structure ValidationErrorMixin {
    @required
    @memberExample("Validation error detail")
    detail: String

    @required
    @const("validation_error_code")
    code: String

    @required
    @memberExample("field")
    ref: String
}

list ValidationErrorList {
    member: ValidationErrorUnion
}

union ValidationErrorUnion {
    missingValueValidationError: MissingValueValidationError
    invalidFormatValidationError: InvalidFormatValidationError
}

structure MissingValueValidationError with [ValidationErrorMixin] {}

structure InvalidFormatValidationError with [ValidationErrorMixin] {}

@error("client")
@httpError(400)
structure ValidationApiErrorExceptionImpl with [ValidationApiErrorException] {
    @memberExample([
        {
            missingValueValidationError: { code: "missing_value", detail: "Name is required", ref: "name" }
        },
        {
            invalidFormatValidationError: {
                code: "invalid_format",
                detail: "Email must be a valid email address",
                ref: "email",
                attributes: { pattern: "^[a-zA-Z0-9]+$" }
            }
        }
    ])
    @required
    errors: ValidationErrorList
}
```

Json example for Validation Error:
```json
{
    "type": "/errors/types/validation",
    "title": "Validation Problem",
    "status": 422,
    "detail": "Validation error detail",
    "instance": "/api/v1/users",
    "errors": [
        {
            "code": "invalid_format",
            "detail": "Email must be a valid email address",
            "ref": "email",
            "attributes": {
                "pattern": "^[a-zA-Z0-9]+$"
            }
        },
        {
            "code": "missing_value",
            "detail": "Name is required",
            "ref": "name"
        }
    ]
}
```

Specific validation error details would look like this:
```smithy
// ------------ InvalidFormat Validation Error ------------
structure InvalidFormatAttributes {
    @memberExample("^[a-zA-Z0-9]+$")
    @required
    pattern: String
}

structure InvalidFormatValidationError with [ValidationErrorMixin] {
    @const("invalid_format")
    @required
    code: String

    @memberExample("Email must be a valid email address")
    @required
    detail: String

    @memberExample("email")
    @required
    ref: String

    attributes: InvalidFormatAttributes
}
```

### Improvements
- We have NOT put too much thoughts on validation and making sure these traits throw errors to warn devs they are doing something we shouldn't
    - Adding a const with the wrong simpleType (string instead of int)
    - Adding an example that doesn't match the schema

## Codegen: Java Spring-boot implementation
Now the juicy bits, the specific SpringBoot implementation for these errors! We follow a simplified flat hierarchy where each exception extends directly from `ApiErrorResponseException` and builds its own `ProblemDetail`.

### Design Principles

1. **Flat Hierarchy**: Only 2 levels of inheritance (Spring's `ErrorResponseException` -> `ApiErrorResponseException` -> concrete exceptions)
2. **Self-Contained**: Each exception builds its own `ProblemDetail` internally via a single `buildProblemDetail` method
3. **Type Safety**: Domain exceptions use typed error code enums and attributes records
4. **Immutability**: Exceptions are immutable, built via builders with private constructors
5. **RFC 7807 Compliant**: All responses follow the Problem Details specification
6. **Error Type Header**: Every exception includes an `x-error-type` header with the class name for easy identification
7. **Direct Property Access**: All exceptions provide getters (getType, getTitle, getStatus, getDetail, getInstance) to avoid accessing getBody() directly
8. **JSON Deserialization**: Each exception has a private `@JsonCreator` constructor for deserializing from JSON responses

### Base Classes

An abstract class (ApiErrorResponseException) will help us identify our errors vs spring errors (ErrorResponseException). It also provides the `x-error-type` response header and common property getters for RFC 7807 fields:
```java
public abstract class ApiErrorResponseException extends ErrorResponseException {

  public static final String ERROR_TYPE_HEADER = "x-error-type";

  protected ApiErrorResponseException(ProblemDetail problemDetail, String errorType) {
    super(HttpStatusCode.valueOf(problemDetail.getStatus()), problemDetail, null);
    getHeaders().add(ERROR_TYPE_HEADER, errorType);
  }

  public URI getType() {
    return getBody().getType();
  }

  public String getTitle() {
    return getBody().getTitle();
  }

  public int getStatus() {
    return getBody().getStatus();
  }

  public String getDetail() {
    return getBody().getDetail();
  }

  public URI getInstance() {
    return getBody().getInstance();
  }
}
```

Each concrete exception defines its error type as a hardcoded constant and passes it to the constructor:
```java
public final class TransferLimitExceededException extends ApiErrorResponseException {
  private static final String ERROR_TYPE = "TransferLimitExceededException";
  // ...

  private TransferLimitExceededException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }
}
```

This header allows clients to programmatically identify the specific error type without parsing the response body:
```
HTTP/1.1 422 Unprocessable Content
Content-Type: application/problem+json
x-error-type: TransferLimitExceededException

{
  "type": "/errors/types/domain",
  "title": "Transfer Limit Exceeded",
  ...
}
```

We are also going to define an "Attributes" interface to make working with error attributes more generic:
```java
public interface ErrorAttributes {

}
```

We also define a base interface for all error codes:
```java
public interface ErrorCode {

  String getCode();
}
```

### Domain Errors
Domain exceptions extend `ApiErrorResponseException` directly and build their own `ProblemDetail`. Each exception has a fixed type, title, status, and code, with configurable detail and type-safe attributes.

We use enums for domain error codes that follow a domain.error_code pattern. The `DomainErrorCode` interface is sealed to only permit specific domain error code enums:
```java
public sealed interface DomainErrorCode extends ErrorCode
    permits TransferErrorCode, AccountErrorCode {

  String getDomain();

  String getErrorCode();
}
```

```java
public enum TransferErrorCode implements DomainErrorCode {

  TRANSFER_LIMIT_EXCEEDED("transfer_limit_exceeded");

  private static final String DOMAIN = "transfer";

  private final String errorCode;
  private final String code;

  TransferErrorCode(String errorCode) {
    this.errorCode = errorCode;
    this.code = String.join(getDelimiter(), DOMAIN, errorCode);
  }

  @Override
  public String getDomain() {
    return DOMAIN;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }

  public static TransferErrorCode valueOfCode(String code) {
    for (TransferErrorCode errorCode : values()) {
      if (errorCode.getCode().equals(code)) {
        return errorCode;
      }
    }
    throw new IllegalArgumentException("No enum constant with code: " + code);
  }
}
```

The specific domain exception implementation (note: common getters like `getType()`, `getTitle()`, etc. are inherited from `ApiErrorResponseException`):
```java
public final class TransferLimitExceededException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "TransferLimitExceededException";
  private static final URI TYPE = URI.create("/errors/types/domain");
  private static final TransferErrorCode CODE = TransferErrorCode.TRANSFER_LIMIT_EXCEEDED;
  private static final String TITLE = "Transfer Limit Exceeded";
  private static final String DETAIL = "Transfer Limit has been exceeded";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNPROCESSABLE_CONTENT;
  private static final String CODE_PROPERTY = "code";
  private static final String ATTRIBUTES_PROPERTY = "attributes";

  private TransferLimitExceededException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private TransferLimitExceededException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") String code,
      @JsonProperty("attributes") TransferLimitExceededAttributes attributes) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance,
        TransferErrorCode.valueOfCode(code), attributes), ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  public TransferErrorCode getCode() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (TransferErrorCode) props.get(CODE_PROPERTY))
        .orElse(CODE);
  }

  public TransferLimitExceededAttributes getAttributes() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (TransferLimitExceededAttributes) props.get(ATTRIBUTES_PROPERTY))
        .orElse(null);
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, TransferErrorCode code,
      TransferLimitExceededAttributes attributes) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title != null ? title : TITLE);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(CODE_PROPERTY, code != null ? code : CODE.getCode());
    problemDetail.setProperty(ATTRIBUTES_PROPERTY, attributes);
    return problemDetail;
  }

  public static final class Builder {

    private TransferLimitExceededAttributes attributes;

    private Builder() {
    }

    public Builder attributes(TransferLimitExceededAttributes attributes) {
      this.attributes = attributes;
      return this;
    }

    public TransferLimitExceededException build() {
      Objects.requireNonNull(attributes, "attributes is required");
      return new TransferLimitExceededException(
          buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, DETAIL, null, CODE, attributes));
    }
  }
}
```

And the type-safe attributes record:
```java
public record TransferLimitExceededAttributes(BigDecimal amount, String currency) implements
    ErrorAttributes {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private BigDecimal amount;
    private String currency;

    private Builder() {
    }

    public Builder amount(BigDecimal amount) {
      this.amount = amount;
      return this;
    }

    public Builder currency(String currency) {
      this.currency = currency;
      return this;
    }

    public TransferLimitExceededAttributes build() {
      return new TransferLimitExceededAttributes(amount, currency);
    }
  }
}
```

Usage example:
```java
throw TransferLimitExceededException.builder()
    .attributes(TransferLimitExceededAttributes.builder()
        .amount(new BigDecimal("15000.00"))
        .currency("USD")
        .build())
    .build();

// JSON Response:
// {
//   "type": "/errors/types/domain",
//   "title": "Transfer Limit Exceeded",
//   "status": 422,
//   "detail": "Transfer Limit has been exceeded",
//   "code": "transfer.transfer_limit_exceeded",
//   "attributes": {
//     "amount": 15000.00,
//     "currency": "USD"
//   }
// }
```

### Validation Errors
Validation errors contain a list of error details rather than specific exceptions for each type:

```java
public final class ValidationErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "ValidationErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/validation");
  private static final String TITLE = "Validation Problem";
  private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
  private static final String ERRORS_PROPERTY = "errors";

  private ValidationErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private ValidationErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("errors") List<ValidationError> errors) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance, errors),
        ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unchecked")
  public List<ValidationError> getErrors() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (List<ValidationError>) props.get(ERRORS_PROPERTY))
        .orElse(Collections.emptyList());
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, List<ValidationError> errors) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title != null ? title : TITLE);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(ERRORS_PROPERTY,
        errors != null ? new ArrayList<>(errors) : new ArrayList<>());
    return problemDetail;
  }

  public static final class Builder {

    private final List<ValidationError> errors = new ArrayList<>();

    private Builder() {
    }

    public Builder error(ValidationError error) {
      this.errors.add(error);
      return this;
    }

    public Builder errors(List<? extends ValidationError> errors) {
      this.errors.addAll(errors);
      return this;
    }

    public ValidationErrorResponseException build() {
      return new ValidationErrorResponseException(
          buildProblemDetail(TYPE, TITLE, STATUS, null, null, errors));
    }
  }
}
```

The ValidationError class allows working with multiple types of validation errors:
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "code")
@JsonSubTypes({
    @JsonSubTypes.Type(value = InvalidFormatValidationError.class, name = "invalid_format"),
    @JsonSubTypes.Type(value = MissingValueValidationError.class, name = "missing_value")
})
public abstract sealed class ValidationError
    permits InvalidFormatValidationError, MissingValueValidationError {

  private final String code;
  private final String detail;
  private final String ref;
  private final ErrorAttributes attributes;

  protected ValidationError(String code, String detail, String ref, ErrorAttributes attributes) {
    this.code = code;
    this.detail = detail;
    this.ref = ref;
    this.attributes = attributes;
  }

  public String getCode() {
    return code;
  }

  public String getDetail() {
    return detail;
  }

  public String getRef() {
    return ref;
  }

  public ErrorAttributes getAttributes() {
    return attributes;
  }

  public abstract static class Builder<A extends ErrorAttributes, T extends ValidationError> {

    protected String detail;
    protected String ref;
    protected A attributes;

    protected Builder() {
    }

    public Builder<A, T> detail(String detail) {
      this.detail = detail;
      return this;
    }

    public Builder<A, T> ref(String ref) {
      this.ref = ref;
      return this;
    }

    public Builder<A, T> attributes(A attributes) {
      this.attributes = attributes;
      return this;
    }

    public abstract T build();
  }
}
```

We also define an enum for validation error codes:
```java
public enum ValidationErrorCode implements ErrorCode {

  MISSING_VALUE("missing_value"),
  INVALID_FORMAT("invalid_format");

  private final String code;

  ValidationErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }
}
```

And the specific implementation for the errors and their error attributes. Note that each validation error type has a private `@JsonCreator` constructor for deserialization:
```java
public final class InvalidFormatValidationError extends ValidationError {

  private static final ValidationErrorCode CODE = ValidationErrorCode.INVALID_FORMAT;

  @JsonCreator
  private InvalidFormatValidationError(
      @JsonProperty("detail") String detail,
      @JsonProperty("ref") String ref,
      @JsonProperty("attributes") InvalidFormatAttributes attributes) {
    super(CODE.getCode(), detail, ref, attributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public InvalidFormatAttributes getAttributes() {
    return (InvalidFormatAttributes) super.getAttributes();
  }

  public static final class Builder
      extends ValidationError.Builder<InvalidFormatAttributes, InvalidFormatValidationError> {

    private Builder() {
    }

    @Override
    public InvalidFormatValidationError build() {
      return new InvalidFormatValidationError(detail, ref, attributes);
    }
  }
}
```

Type-safe attributes for invalid format errors:
```java
public record InvalidFormatAttributes(String pattern) implements ErrorAttributes {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private String pattern;

    private Builder() {
    }

    public Builder pattern(String pattern) {
      this.pattern = pattern;
      return this;
    }

    public InvalidFormatAttributes build() {
      return new InvalidFormatAttributes(pattern);
    }
  }
}
```

Usage example:
```java
throw ValidationErrorResponseException.builder()
    .error(InvalidFormatValidationError.builder()
        .detail("Email format is invalid")
        .ref("user.email")
        .attributes(InvalidFormatAttributes.builder()
            .pattern("^[\\w.-]+@[\\w.-]+\\.\\w+$")
            .build())
        .build())
    .error(MissingValueValidationError.builder()
        .detail("First name is required")
        .ref("user.firstName")
        .attributes(MissingValueAttributes.builder()
            .missingField("firstName")
            .build())
        .build())
    .build();

// JSON Response:
// {
//   "type": "/errors/types/validation",
//   "title": "Validation Problem",
//   "status": 400,
//   "errors": [
//     {
//       "code": "invalid_format",
//       "detail": "Email format is invalid",
//       "ref": "user.email",
//       "attributes": { "pattern": "^[\\w.-]+@[\\w.-]+\\.\\w+$" }
//     },
//     {
//       "code": "missing_value",
//       "detail": "First name is required",
//       "ref": "user.firstName",
//       "attributes": { "missingField": "firstName" }
//     }
//   ]
// }
```

### Access Errors
Access errors handle authentication and authorization failures. They include a typed error code and have all values defined as static constants:

```java
public enum AccessErrorCode implements ErrorCode {

  UNAUTHORIZED("unauthorized");

  private final String code;

  AccessErrorCode(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String toString() {
    return code;
  }

  public static AccessErrorCode valueOfCode(String code) {
    for (AccessErrorCode errorCode : values()) {
      if (errorCode.getCode().equals(code)) {
        return errorCode;
      }
    }
    throw new IllegalArgumentException("No enum constant with code: " + code);
  }
}
```

```java
public final class InvalidTokenAccessErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "InvalidTokenAccessErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/access");
  private static final String TITLE = "Access Error";
  private static final String DETAIL = "Invalid Token";
  private static final AccessErrorCode CODE = AccessErrorCode.UNAUTHORIZED;
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.UNAUTHORIZED;
  private static final String CODE_PROPERTY = "code";

  private InvalidTokenAccessErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private InvalidTokenAccessErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance,
      @JsonProperty("code") String code) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance,
        code != null ? AccessErrorCode.valueOfCode(code) : CODE), ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  public AccessErrorCode getCode() {
    return Optional.ofNullable(getBody().getProperties())
        .map(props -> (AccessErrorCode) props.get(CODE_PROPERTY))
        .orElse(CODE);
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance, AccessErrorCode code) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    problemDetail.setProperty(CODE_PROPERTY, code != null ? code : CODE);
    return problemDetail;
  }

  public static final class Builder {

    private Builder() {
    }

    public InvalidTokenAccessErrorResponseException build() {
      return new InvalidTokenAccessErrorResponseException(
          buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, DETAIL, null, CODE));
    }
  }
}
```

Usage example:
```java
throw InvalidTokenAccessErrorResponseException.builder().build();

// JSON Response:
// {
//   "type": "/errors/types/access",
//   "title": "Access Error",
//   "status": 401,
//   "detail": "Invalid Token",
//   "code": "unauthorized"
// }
```

### Server Errors
Server errors handle internal server failures. The title is a static constant:

```java
public final class InternalServerErrorResponseException extends ApiErrorResponseException {

  private static final String ERROR_TYPE = "InternalServerErrorResponseException";
  private static final URI TYPE = URI.create("/errors/types/server");
  private static final String TITLE = "Internal Server Error";
  private static final HttpStatus DEFAULT_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

  private InternalServerErrorResponseException(ProblemDetail problemDetail) {
    super(problemDetail, ERROR_TYPE);
  }

  @JsonCreator
  private InternalServerErrorResponseException(
      @JsonProperty("type") URI type,
      @JsonProperty("title") String title,
      @JsonProperty("status") int status,
      @JsonProperty("detail") String detail,
      @JsonProperty("instance") String instance) {
    super(buildProblemDetail(type, title, HttpStatus.valueOf(status), detail, instance),
        ERROR_TYPE);
  }

  public static Builder builder() {
    return new Builder();
  }

  private static ProblemDetail buildProblemDetail(URI type, String title, HttpStatus status,
      String detail, String instance) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(status);
    problemDetail.setType(type != null ? type : TYPE);
    problemDetail.setTitle(title);
    if (detail != null) {
      problemDetail.setDetail(detail);
    }
    if (instance != null) {
      problemDetail.setInstance(URI.create(instance));
    }
    return problemDetail;
  }

  public static final class Builder {

    private String detail;

    private Builder() {
    }

    public Builder detail(String detail) {
      this.detail = detail;
      return this;
    }

    public InternalServerErrorResponseException build() {
      return new InternalServerErrorResponseException(
          buildProblemDetail(TYPE, TITLE, DEFAULT_STATUS, detail, null));
    }
  }
}
```

Usage example:
```java
throw InternalServerErrorResponseException.builder()
    .detail("Database connection failed")
    .build();

// JSON Response:
// {
//   "type": "/errors/types/server",
//   "title": "Internal Server Error",
//   "status": 500,
//   "detail": "Database connection failed"
// }
```

### Client-Side Deserialization
For client-side usage, we provide an `ApiErrorResponseDeserializer` that uses the `x-error-type` header to deserialize JSON responses into the correct exception type:

```java
public class ApiErrorResponseDeserializer {

  private static final Map<String, Class<? extends ApiErrorResponseException>> EXCEPTION_TYPES =
      Map.of(
          "TransferLimitExceededException", TransferLimitExceededException.class,
          "AccountSuspendedException", AccountSuspendedException.class,
          "ValidationErrorResponseException", ValidationErrorResponseException.class,
          "InvalidTokenAccessErrorResponseException", InvalidTokenAccessErrorResponseException.class,
          "InternalServerErrorResponseException", InternalServerErrorResponseException.class
      );

  private final ObjectMapper objectMapper;

  public ApiErrorResponseDeserializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  public <T extends ApiErrorResponseException> T deserialize(String json, String errorTypeHeader)
      throws JacksonException {
    Class<? extends ApiErrorResponseException> targetClass = EXCEPTION_TYPES.get(errorTypeHeader);
    if (targetClass == null) {
      throw new IllegalArgumentException("Unknown error type: " + errorTypeHeader);
    }
    return (T) objectMapper.readValue(json, targetClass);
  }

  public <T extends ApiErrorResponseException> T deserialize(String json, Class<T> targetClass)
      throws JacksonException {
    return objectMapper.readValue(json, targetClass);
  }

  public Class<? extends ApiErrorResponseException> getExceptionClass(String errorTypeHeader) {
    return EXCEPTION_TYPES.get(errorTypeHeader);
  }
}
```

Usage example:
```java
// Using HTTP client and reading the x-error-type header
String json = response.body();
String errorType = response.headers().firstValue("x-error-type").orElse(null);

ApiErrorResponseDeserializer deserializer = new ApiErrorResponseDeserializer(objectMapper);
TransferLimitExceededException exception = deserializer.deserialize(json, errorType);

// Type-safe access to all properties
TransferErrorCode code = exception.getCode();
TransferLimitExceededAttributes attrs = exception.getAttributes();
BigDecimal amount = attrs.amount();
String currency = attrs.currency();
```

### File Structure
```
exception/
├── ApiErrorResponseException.java       # Base class
├── ApiErrorResponseDeserializer.java    # Client-side deserializer
├── ErrorAttributes.java                 # Marker interface
├── ErrorCode.java                       # Error code interface
├── access/
│   ├── AccessErrorCode.java
│   └── InvalidTokenAccessErrorResponseException.java
├── domain/
│   ├── AccountErrorCode.java
│   ├── AccountSuspendedAttributes.java
│   ├── AccountSuspendedException.java
│   ├── DomainErrorCode.java
│   ├── TransferErrorCode.java
│   ├── TransferLimitExceededAttributes.java
│   └── TransferLimitExceededException.java
├── server/
│   └── InternalServerErrorResponseException.java
└── validation/
    ├── InvalidFormatAttributes.java
    ├── InvalidFormatValidationError.java
    ├── MissingValueAttributes.java
    ├── MissingValueValidationError.java
    ├── ValidationError.java
    ├── ValidationErrorCode.java
    └── ValidationErrorResponseException.java
```

### Summary

| Aspect | Value |
|--------|-------|
| Hierarchy depth | 2 levels |
| Exception classes | 5 |
| ProblemDetail classes | 0 (built internally) |
| Code complexity | Low |
| Exception creation | Single-step builder |
| Deserialization | Via `@JsonCreator` + `ApiErrorResponseDeserializer` |

### Key Implementation Patterns

1. **Single `buildProblemDetail` method**: Each exception has one static method that handles both builder construction and JSON deserialization, ensuring consistent behavior.

2. **Private constructors**: All constructors are private - use the builder for creation and `@JsonCreator` for deserialization.

3. **Typed error codes**: Domain exceptions return typed enums (e.g., `TransferErrorCode`) from `getCode()`, not strings. Use `code.getCode()` to get the string value.

4. **Direct property access**: Common getters (`getType()`, `getTitle()`, `getStatus()`, `getDetail()`, `getInstance()`) are defined in `ApiErrorResponseException` base class to avoid accessing `getBody()` directly.

5. **`valueOfCode` for deserialization**: Each error code enum provides a `valueOfCode(String)` method to convert JSON string codes back to enum values.

6. **Sealed types**: `DomainErrorCode` and `ValidationError` use sealed interfaces/classes to restrict implementations.

### Improvements
This is a proposal, we would appreciate any simplification possible but our aim is to have an extensible solution and also allow us to be very specific when handling these events on the client side (future SDK).

Error codes have been abstracted using enums implementing the `ErrorCode` interface. Domain errors use `DomainErrorCode` which provides a domain prefix pattern (e.g., `transfer.transfer_limit_exceeded`), while validation errors use `ValidationErrorCode` for simpler codes (e.g., `invalid_format`, `missing_value`).

The builder pattern has been adopted throughout for a fluent and type-safe API. Domain exception builders enforce mandatory attributes at build time using `Objects.requireNonNull()`.

We implemented comprehensive tests to validate Jackson serialization and deserialization (including round-trip tests) so the code should be fairly usable. Using this context with an LLM will probably give us a good head start if agreed upon the proposed solution.
