# Context
We want to generate Spring problem detail implementation for errors. For this, we want to take inspiration from our smithy error implementation in `/smithy-poc/example-service/model/errors`.

We want to create an exception `ApiErrorResponseException` that extends spring boot `ErrorResponseException` and then we are going to have the four exception types extending from our domain `ApiErrorResponseException`:
- AccessErrorResponseException
- ServerErrorResponseException
- ValidationErrorResponseException
- DomainErrorResponseException

The difference between these exception is basically which type of `problemDetail` implementation they allow. For example, `AccessErrorResponseException` will only allow `AccessProblemDetail` and so on. For this, we are also creating an extension from `ProblemDetail` for each of the four types of exceptions:
- AccessProblemDetail
- ServerProblemDetail
- ValidationProblemDetail
- DomainProblemDetail

Then finally, we are going to have the specific implementations for the concrete errors:
- TransferLimitExceededProblemDetail
- AccountSuspendedProblemDetail

Which are going to have type safe attributes associated  to them
- TransferLimitExceededProblemDetail: TransferLimitExceededAttributes(amount: BigDecimal, currency: String)
- AccountSuspendedProblemDetail: - AccountSuspendedAttributes(reason: String)

Validation as described in the validaiton-error-mixin.smithy is a bit more complex since `ValidationProblemDetail` is going to have a list of `ValidationError`. These validaiton error are going to be implemented with more concrete classes such as:
- InvalidFormatValidationError
  - code
  - details
  - ref
  - attributes (InvalidFormatValidationError(pattern: String))
- MissingValueValidationError
    - code
    - details
    - ref
    - attributes(MissingValueValidationErrorAttributes(missingField: String))

# Task
- Plan an implementation detail for the above exceptions. Simply use Spring and java to implement the structure and do not use anything smithy related, only use smithy as a guide for the structure of these classes.
- Add spring boot dependencies to allow using the `ErrorResponseException` and `ProblemDetail` classes.