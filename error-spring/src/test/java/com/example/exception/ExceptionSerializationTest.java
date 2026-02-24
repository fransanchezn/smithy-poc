package com.example.exception;

import com.example.exception.access.AccessErrorResponseException;
import com.example.exception.domain.AccountSuspendedException;
import com.example.exception.domain.TransferLimitExceededException;
import com.example.exception.server.ServerErrorResponseException;
import com.example.exception.validation.InvalidFormatException;
import com.example.exception.validation.MissingValueException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

import java.math.BigDecimal;

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
        void shouldSerializeUnauthorizedException() throws Exception {
            AccessErrorResponseException exception = AccessErrorResponseException.unauthorized("Invalid token");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/access-error");
            assertThat(node.get("title").asText()).isEqualTo("Unauthorized");
            assertThat(node.get("status").asInt()).isEqualTo(401);
            assertThat(node.get("detail").asText()).isEqualTo("Invalid token");
        }

        @Test
        void shouldSerializeForbiddenException() throws Exception {
            AccessErrorResponseException exception = AccessErrorResponseException.forbidden("Insufficient permissions");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/access-error");
            assertThat(node.get("title").asText()).isEqualTo("Forbidden");
            assertThat(node.get("status").asInt()).isEqualTo(403);
            assertThat(node.get("detail").asText()).isEqualTo("Insufficient permissions");
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
        void shouldSerializeInternalServerErrorException() throws Exception {
            ServerErrorResponseException exception = ServerErrorResponseException.internalServerError("Database connection failed");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/server-error");
            assertThat(node.get("title").asText()).isEqualTo("Internal Server Error");
            assertThat(node.get("status").asInt()).isEqualTo(500);
            assertThat(node.get("detail").asText()).isEqualTo("Database connection failed");
        }

        @Test
        void shouldSerializeServiceUnavailableException() throws Exception {
            ServerErrorResponseException exception = ServerErrorResponseException.serviceUnavailable("Service is under maintenance");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/server-error");
            assertThat(node.get("title").asText()).isEqualTo("Service Unavailable");
            assertThat(node.get("status").asInt()).isEqualTo(503);
            assertThat(node.get("detail").asText()).isEqualTo("Service is under maintenance");
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
        void shouldSerializeTransferLimitExceededException() throws Exception {
            TransferLimitExceededException exception = new TransferLimitExceededException(
                    "Your transfer exceeds the daily limit",
                    new BigDecimal("50000.00"),
                    "EUR"
            );

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/domain-error");
            assertThat(node.get("title").asText()).isEqualTo("Transfer Limit Exceeded");
            assertThat(node.get("status").asInt()).isEqualTo(422);
            assertThat(node.get("detail").asText()).isEqualTo("Your transfer exceeds the daily limit");
            assertThat(node.get("code").asText()).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(node.get("attributes").get("amount").decimalValue()).isEqualByComparingTo("50000.00");
            assertThat(node.get("attributes").get("currency").asText()).isEqualTo("EUR");
        }

        @Test
        void shouldSerializeAccountSuspendedException() throws Exception {
            AccountSuspendedException exception = new AccountSuspendedException(
                    "Account access denied",
                    "Multiple failed login attempts"
            );

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/domain-error");
            assertThat(node.get("title").asText()).isEqualTo("Account Suspended");
            assertThat(node.get("status").asInt()).isEqualTo(422);
            assertThat(node.get("detail").asText()).isEqualTo("Account access denied");
            assertThat(node.get("code").asText()).isEqualTo("ACCOUNT_SUSPENDED");
            assertThat(node.get("attributes").get("reason").asText()).isEqualTo("Multiple failed login attempts");
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
        void shouldSerializeInvalidFormatException() throws Exception {
            InvalidFormatException exception = new InvalidFormatException()
                    .addError("Email format is invalid", "user.email", "^[\\w.-]+@[\\w.-]+\\.\\w+$")
                    .addError("Date format is invalid", "user.birthDate", "^\\d{4}-\\d{2}-\\d{2}$");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("/errors/types/validation");
            assertThat(node.get("title").asText()).isEqualTo("Validation Problem");
            assertThat(node.get("status").asInt()).isEqualTo(400);
            assertThat(node.get("errors").size()).isEqualTo(2);

            JsonNode firstError = node.get("errors").get(0);
            assertThat(firstError.get("code").asText()).isEqualTo("invalid_format");
            assertThat(firstError.get("detail").asText()).isEqualTo("Email format is invalid");
            assertThat(firstError.get("ref").asText()).isEqualTo("user.email");
        }

        @Test
        void shouldSerializeMissingValueException() throws Exception {
            MissingValueException exception = new MissingValueException()
                    .addError("First name is required", "firstName", "firstName")
                    .addError("Last name is required", "lastName", "lastName");

            String json = objectMapper.writeValueAsString(exception.getBody());
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("/errors/types/validation");
            assertThat(node.get("title").asText()).isEqualTo("Validation Problem");
            assertThat(node.get("status").asInt()).isEqualTo(400);
            assertThat(node.get("errors").size()).isEqualTo(2);

            JsonNode firstError = node.get("errors").get(0);
            assertThat(firstError.get("code").asText()).isEqualTo("missing_value");
            assertThat(firstError.get("detail").asText()).isEqualTo("First name is required");
        }

        @Test
        void shouldProvideTypedAccessToErrors() {
            InvalidFormatException exception = new InvalidFormatException()
                    .addError("Invalid email", "email", "^[a-z]+@[a-z]+$");

            assertThat(exception.getErrors()).hasSize(1);
            assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
            assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("Invalid email");
            assertThat(exception.getErrors().get(0).getRef()).isEqualTo("email");
        }

        @Test
        void shouldSupportFluentApi() {
            MissingValueException exception = new MissingValueException()
                    .addError("Field 1 required", "field1", "field1")
                    .addError("Field 2 required", "field2", "field2")
                    .addError("Field 3 required", "field3", "field3");

            assertThat(exception.getErrors()).hasSize(3);
        }
    }
}
