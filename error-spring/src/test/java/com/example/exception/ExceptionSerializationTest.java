package com.example.exception;

import com.example.exception.access.AccessErrorResponseException;
import com.example.exception.access.AccessProblemDetail;
import com.example.exception.domain.AccountSuspendedException;
import com.example.exception.domain.AccountSuspendedProblemDetail;
import com.example.exception.domain.TransferLimitExceededException;
import com.example.exception.domain.TransferLimitExceededProblemDetail;
import com.example.exception.server.ServerErrorResponseException;
import com.example.exception.server.ServerProblemDetail;
import com.example.exception.validation.ValidationErrorResponseException;
import com.example.exception.validation.ValidationProblemDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);
    }

    @Nested
    class AccessExceptionTest {

        @Test
        void shouldSerializeAndDeserializeUnauthorizedException() throws Exception {
            AccessErrorResponseException exception = AccessErrorResponseException.unauthorized("Invalid token");

            String json = objectMapper.writeValueAsString(exception.getBody());
            AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/access-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Unauthorized");
            assertThat(deserialized.getStatus()).isEqualTo(401);
            assertThat(deserialized.getDetail()).isEqualTo("Invalid token");
        }

        @Test
        void shouldSerializeAndDeserializeForbiddenException() throws Exception {
            AccessErrorResponseException exception = AccessErrorResponseException.forbidden("Insufficient permissions");

            String json = objectMapper.writeValueAsString(exception.getBody());
            AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/access-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Forbidden");
            assertThat(deserialized.getStatus()).isEqualTo(403);
            assertThat(deserialized.getDetail()).isEqualTo("Insufficient permissions");
        }

        @Test
        void shouldHaveCorrectStatusCode() {
            AccessErrorResponseException unauthorized = AccessErrorResponseException.unauthorized("test");
            AccessErrorResponseException forbidden = AccessErrorResponseException.forbidden("test");

            assertThat(unauthorized.getStatusCode().value()).isEqualTo(401);
            assertThat(forbidden.getStatusCode().value()).isEqualTo(403);
        }
    }

    @Nested
    class ServerExceptionTest {

        @Test
        void shouldSerializeAndDeserializeInternalServerErrorException() throws Exception {
            ServerErrorResponseException exception = ServerErrorResponseException.internalServerError("Database connection failed");

            String json = objectMapper.writeValueAsString(exception.getBody());
            ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/server-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Internal Server Error");
            assertThat(deserialized.getStatus()).isEqualTo(500);
            assertThat(deserialized.getDetail()).isEqualTo("Database connection failed");
        }

        @Test
        void shouldSerializeAndDeserializeServiceUnavailableException() throws Exception {
            ServerErrorResponseException exception = ServerErrorResponseException.serviceUnavailable("Service is under maintenance");

            String json = objectMapper.writeValueAsString(exception.getBody());
            ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/server-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Service Unavailable");
            assertThat(deserialized.getStatus()).isEqualTo(503);
            assertThat(deserialized.getDetail()).isEqualTo("Service is under maintenance");
        }

        @Test
        void shouldPreserveCause() {
            RuntimeException cause = new RuntimeException("Original error");
            ServerErrorResponseException exception = ServerErrorResponseException.internalServerError("Wrapped error", cause);

            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    class DomainExceptionTest {

        @Test
        void shouldSerializeAndDeserializeTransferLimitExceededException() throws Exception {
            TransferLimitExceededException exception = new TransferLimitExceededException(
                    "Your transfer exceeds the daily limit",
                    new BigDecimal("50000.00"),
                    "EUR"
            );

            String json = objectMapper.writeValueAsString(exception.getBody());
            TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json, TransferLimitExceededProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/domain-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Transfer Limit Exceeded");
            assertThat(deserialized.getStatus()).isEqualTo(422);
            assertThat(deserialized.getDetail()).isEqualTo("Your transfer exceeds the daily limit");
            assertThat(deserialized.getCode()).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo("50000.00");
            assertThat(deserialized.getAttributes().currency()).isEqualTo("EUR");
        }

        @Test
        void shouldSerializeAndDeserializeAccountSuspendedException() throws Exception {
            AccountSuspendedException exception = new AccountSuspendedException(
                    "Account access denied",
                    "Multiple failed login attempts"
            );

            String json = objectMapper.writeValueAsString(exception.getBody());
            AccountSuspendedProblemDetail deserialized = objectMapper.readValue(json, AccountSuspendedProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("https://errors.example.com/domain-error"));
            assertThat(deserialized.getTitle()).isEqualTo("Account Suspended");
            assertThat(deserialized.getStatus()).isEqualTo(422);
            assertThat(deserialized.getDetail()).isEqualTo("Account access denied");
            assertThat(deserialized.getCode()).isEqualTo("ACCOUNT_SUSPENDED");
            assertThat(deserialized.getAttributes().reason()).isEqualTo("Multiple failed login attempts");
        }

        @Test
        void shouldProvideTypedAccessToAttributes() {
            TransferLimitExceededException exception = new TransferLimitExceededException(
                    "Test",
                    new BigDecimal("1000"),
                    "USD"
            );

            assertThat(exception.getCode()).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(exception.getAttributes().amount()).isEqualByComparingTo("1000");
            assertThat(exception.getAttributes().currency()).isEqualTo("USD");
        }
    }

    @Nested
    class ValidationExceptionTest {

        @Test
        void shouldSerializeAndDeserializeInvalidFormatErrors() throws Exception {
            ValidationErrorResponseException exception = new ValidationErrorResponseException()
                    .addInvalidFormat("Email format is invalid", "user.email", "^[\\w.-]+@[\\w.-]+\\.\\w+$")
                    .addInvalidFormat("Date format is invalid", "user.birthDate", "^\\d{4}-\\d{2}-\\d{2}$");

            String json = objectMapper.writeValueAsString(exception.getBody());
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
            assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
            assertThat(deserialized.getStatus()).isEqualTo(400);
            assertThat(deserialized.getErrors()).hasSize(2);

            assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
            assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("Email format is invalid");
            assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo("user.email");
        }

        @Test
        void shouldSerializeAndDeserializeMissingValueErrors() throws Exception {
            ValidationErrorResponseException exception = new ValidationErrorResponseException()
                    .addMissingValue("First name is required", "firstName", "firstName")
                    .addMissingValue("Last name is required", "lastName", "lastName");

            String json = objectMapper.writeValueAsString(exception.getBody());
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
            assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
            assertThat(deserialized.getStatus()).isEqualTo(400);
            assertThat(deserialized.getErrors()).hasSize(2);

            assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("missing_value");
            assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("First name is required");
        }

        @Test
        void shouldSerializeAndDeserializeMixedErrors() throws Exception {
            ValidationErrorResponseException exception = new ValidationErrorResponseException()
                    .addInvalidFormat("Invalid email", "email", "^[a-z]+@[a-z]+$")
                    .addMissingValue("Name is required", "name", "name");

            String json = objectMapper.writeValueAsString(exception.getBody());
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getErrors()).hasSize(2);
            assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
            assertThat(deserialized.getErrors().get(1).getCode()).isEqualTo("missing_value");
        }

        @Test
        void shouldProvideTypedAccessToErrors() {
            ValidationErrorResponseException exception = ValidationErrorResponseException
                    .invalidFormat("Invalid email", "email", "^[a-z]+@[a-z]+$");

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
            assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("Invalid email");
            assertThat(exception.getErrors().get(0).getRef()).isEqualTo("email");
        }

        @Test
        void shouldSupportFluentApi() {
            ValidationErrorResponseException exception = new ValidationErrorResponseException()
                    .addMissingValue("Field 1 required", "field1", "field1")
                    .addMissingValue("Field 2 required", "field2", "field2")
                    .addMissingValue("Field 3 required", "field3", "field3");

            assertThat(exception.getErrors()).hasSize(3);
        }
    }

    @Nested
    class RoundTripTest {

        @Test
        void shouldRoundTripDomainException() throws Exception {
            TransferLimitExceededException original = new TransferLimitExceededException(
                    "Transfer limit exceeded",
                    new BigDecimal("25000"),
                    "GBP"
            );

            String json = objectMapper.writeValueAsString(original.getBody());
            TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json, TransferLimitExceededProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
            assertThat(deserialized.getDetail()).isEqualTo(original.getBody().getDetail());
            assertThat(deserialized.getCode()).isEqualTo(original.getCode());
            assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo(original.getAttributes().amount());
            assertThat(deserialized.getAttributes().currency()).isEqualTo(original.getAttributes().currency());
        }

        @Test
        void shouldRoundTripValidationException() throws Exception {
            ValidationErrorResponseException original = ValidationErrorResponseException
                    .invalidFormat("Invalid format", "field", "pattern");

            String json = objectMapper.writeValueAsString(original.getBody());
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
            assertThat(deserialized.getErrors()).hasSize(original.getErrors().size());
            assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo(original.getErrors().get(0).getCode());
            assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo(original.getErrors().get(0).getDetail());
            assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo(original.getErrors().get(0).getRef());
        }
    }
}
