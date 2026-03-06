# Exception Architecture

This document describes the simplified exception hierarchy used in the error-spring module.

## Overview

The exception system follows RFC 7807 Problem Details for HTTP APIs, providing structured error responses while maintaining a simple, flat inheritance hierarchy.

## Architecture

### Class Diagram

```
┌─────────────────────────────────┐
│   ErrorResponseException        │  (Spring Framework)
│         (abstract)              │
└───────────────┬─────────────────┘
                │
                ▼
┌─────────────────────────────────┐
│   ApiErrorResponseException     │  Base for all API errors
│         (abstract)              │
└───────────────┬─────────────────┘
                │
    ┌───────────┼───────────┬──────────────┬──────────────┐
    │           │           │              │              │
    ▼           ▼           ▼              ▼              ▼
┌─────────┐ ┌─────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│Transfer │ │Account  │ │Validation│ │ Access   │ │ Server   │
│Limit    │ │Suspended│ │Error     │ │Error     │ │Error     │
│Exceeded │ │Exception│ │Response  │ │Response  │ │Response  │
│Exception│ │ (final) │ │Exception │ │Exception │ │Exception │
│ (final) │ │         │ │ (final)  │ │ (final)  │ │ (final)  │
└─────────┘ └─────────┘ └──────────┘ └──────────┘ └──────────┘
```

### Design Principles

1. **Flat Hierarchy**: Only 2 levels of inheritance (Spring's `ErrorResponseException` -> `ApiErrorResponseException` -> concrete exceptions)
2. **Self-Contained**: Each exception builds its own `ProblemDetail`
3. **Type Safety**: Domain exceptions use typed attributes records
4. **Immutability**: Exceptions are immutable, built via builders
5. **RFC 7807 Compliant**: All responses follow the Problem Details specification

## Exception Categories

### Domain Exceptions

Domain exceptions represent business rule violations. They include:
- A unique error code (e.g., `transfer.transfer_limit_exceeded`)
- Type-safe attributes specific to the error

**Available Domain Exceptions:**
- `TransferLimitExceededException` - When a transfer exceeds allowed limits
- `AccountSuspendedException` - When an account is suspended

### Validation Exception

`ValidationErrorResponseException` handles input validation failures. It contains a list of `ValidationError` objects, each with:
- Error code (`invalid_format`, `missing_value`)
- Detail message
- Field reference
- Type-safe attributes

### Access Exception

`AccessErrorResponseException` handles authentication and authorization errors:
- HTTP 401 Unauthorized
- HTTP 403 Forbidden

### Server Exception

`ServerErrorResponseException` handles server-side failures:
- HTTP 500 Internal Server Error
- HTTP 503 Service Unavailable

## Usage Examples

### Domain Exception

```java
// Transfer limit exceeded
throw TransferLimitExceededException.builder()
    .detail("Your transfer exceeds the daily limit of $10,000")
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
//   "detail": "Your transfer exceeds the daily limit of $10,000",
//   "code": "transfer.transfer_limit_exceeded",
//   "attributes": {
//     "amount": 15000.00,
//     "currency": "USD"
//   }
// }
```

### Validation Exception

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

### Access Exception

```java
throw AccessErrorResponseException.builder()
    .title("Unauthorized")
    .detail("Invalid or expired token")
    .build();

// JSON Response:
// {
//   "type": "/errors/types/access",
//   "title": "Unauthorized",
//   "status": 401,
//   "detail": "Invalid or expired token"
// }
```

### Server Exception

```java
throw ServerErrorResponseException.builder()
    .title("Internal Server Error")
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

## Adding New Domain Exceptions

To add a new domain exception:

1. **Create an attributes record** (if needed):
```java
public record InsufficientFundsAttributes(
    BigDecimal available,
    BigDecimal required,
    String currency
) implements ErrorAttributes {
  // Builder...
}
```

2. **Create an error code enum** (if new domain):
```java
public enum FundsErrorCode implements DomainErrorCode {
    INSUFFICIENT_FUNDS("insufficient_funds");
    // ...
}
```

3. **Create the exception**:
```java
public final class InsufficientFundsException extends ApiErrorResponseException {
    private static final URI TYPE = URI.create("/errors/types/domain");
    private static final FundsErrorCode CODE = FundsErrorCode.INSUFFICIENT_FUNDS;
    private static final String TITLE = "Insufficient Funds";
    // ... builder pattern similar to TransferLimitExceededException
}
```

## File Structure

```
exception/
├── ApiErrorResponseException.java       # Base class
├── ErrorAttributes.java                 # Marker interface
├── ErrorCode.java                       # Error code interface
├── access/
│   └── AccessErrorResponseException.java
├── domain/
│   ├── AccountErrorCode.java
│   ├── AccountSuspendedAttributes.java
│   ├── AccountSuspendedException.java
│   ├── DomainErrorCode.java
│   ├── TransferErrorCode.java
│   ├── TransferLimitExceededAttributes.java
│   └── TransferLimitExceededException.java
├── server/
│   └── ServerErrorResponseException.java
└── validation/
    ├── InvalidFormatAttributes.java
    ├── InvalidFormatValidationError.java
    ├── MissingValueAttributes.java
    ├── MissingValueValidationError.java
    ├── ValidationError.java
    ├── ValidationErrorCode.java
    └── ValidationErrorResponseException.java
```

## Migration from Previous Architecture

The previous architecture used a deep hierarchy with intermediate classes:

```
# Before (4-5 levels)
ErrorResponseException
  └── ApiErrorResponseException
      └── DomainErrorResponseException (sealed)
          └── PublicDomainErrorResponseException (sealed)
              └── TransferLimitExceededException (final)

# After (2 levels)
ErrorResponseException
  └── ApiErrorResponseException
      └── TransferLimitExceededException (final)
```

### API Changes

**Before:**
```java
TransferLimitExceededException.builder()
    .problemDetail(TransferLimitExceededProblemDetail.builder()
        .detail("Transfer exceeds limit")
        .attributes(TransferLimitExceededAttributes.builder()
            .amount(new BigDecimal("15000"))
            .currency("USD")
            .build())
        .build())
    .build();
```

**After:**
```java
TransferLimitExceededException.builder()
    .detail("Transfer exceeds limit")
    .attributes(TransferLimitExceededAttributes.builder()
        .amount(new BigDecimal("15000"))
        .currency("USD")
        .build())
    .build();
```

### Deleted Classes

The following classes were removed in the simplification:
- `DomainErrorResponseException`
- `PublicDomainErrorResponseException`
- `InternalDomainErrorResponseException`
- `DomainProblemDetail`
- `TransferLimitExceededProblemDetail`
- `AccountSuspendedProblemDetail`
- `ValidationProblemDetail`
- `PublicValidationErrorResponseException`
- `InternalValidationErrorResponseException`
- `AccessProblemDetail`
- `PublicAccessErrorResponseException`
- `InternalAccessErrorResponseException`
- `ServerProblemDetail`
- `PublicServerErrorResponseException`
- `InternalServerErrorResponseException`

## Benefits

| Aspect | Before | After |
|--------|--------|-------|
| Hierarchy depth | 4-5 levels | 2 levels |
| Exception classes | 18 | 5 |
| ProblemDetail classes | 6 | 0 |
| Code complexity | High | Low |
| Exception creation | Two-step (ProblemDetail + Exception) | Single-step |
| Understanding | Requires understanding sealed hierarchy | Simple flat structure |
