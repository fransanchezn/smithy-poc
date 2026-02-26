package com.example.exception;

import com.example.exception.access.AccessProblemDetail;
import com.example.exception.domain.AccountSuspendedProblemDetail;
import com.example.exception.domain.TransferLimitExceededProblemDetail;
import com.example.exception.server.ServerProblemDetail;
import com.example.exception.validation.InvalidFormatValidationError;
import com.example.exception.validation.MissingValueValidationError;
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

class ProblemDetailSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class);
    }

    @Nested
    class AccessProblemDetailTest {

        @Test
        void shouldSerializeAndDeserializeUnauthorized() throws Exception {
            AccessProblemDetail original = AccessProblemDetail.unauthorized("Invalid credentials");

            String json = objectMapper.writeValueAsString(original);
            AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
            assertThat(deserialized.getTitle()).isEqualTo("Unauthorized");
            assertThat(deserialized.getStatus()).isEqualTo(401);
            assertThat(deserialized.getDetail()).isEqualTo("Invalid credentials");
        }

        @Test
        void shouldSerializeAndDeserializeForbidden() throws Exception {
            AccessProblemDetail original = AccessProblemDetail.forbidden("Access denied");

            String json = objectMapper.writeValueAsString(original);
            AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
            assertThat(deserialized.getTitle()).isEqualTo("Forbidden");
            assertThat(deserialized.getStatus()).isEqualTo(403);
            assertThat(deserialized.getDetail()).isEqualTo("Access denied");
        }
    }

    @Nested
    class ServerProblemDetailTest {

        @Test
        void shouldSerializeAndDeserializeInternalServerError() throws Exception {
            ServerProblemDetail original = ServerProblemDetail.internalServerError("Something went wrong");

            String json = objectMapper.writeValueAsString(original);
            ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/server"));
            assertThat(deserialized.getTitle()).isEqualTo("Internal Server Error");
            assertThat(deserialized.getStatus()).isEqualTo(500);
            assertThat(deserialized.getDetail()).isEqualTo("Something went wrong");
        }

        @Test
        void shouldSerializeAndDeserializeServiceUnavailable() throws Exception {
            ServerProblemDetail original = ServerProblemDetail.serviceUnavailable("Service is down");

            String json = objectMapper.writeValueAsString(original);
            ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/server"));
            assertThat(deserialized.getTitle()).isEqualTo("Service Unavailable");
            assertThat(deserialized.getStatus()).isEqualTo(503);
            assertThat(deserialized.getDetail()).isEqualTo("Service is down");
        }
    }

    @Nested
    class DomainProblemDetailTest {

        @Test
        void shouldSerializeAndDeserializeTransferLimitExceeded() throws Exception {
            TransferLimitExceededProblemDetail original = new TransferLimitExceededProblemDetail(
                    "Transfer amount exceeds your daily limit",
                    new BigDecimal("15000.00"),
                    "USD"
            );

            String json = objectMapper.writeValueAsString(original);
            TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json, TransferLimitExceededProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
            assertThat(deserialized.getTitle()).isEqualTo("Transfer Limit Exceeded");
            assertThat(deserialized.getStatus()).isEqualTo(422);
            assertThat(deserialized.getDetail()).isEqualTo("Transfer amount exceeds your daily limit");
            assertThat(deserialized.getCode()).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo("15000.00");
            assertThat(deserialized.getAttributes().currency()).isEqualTo("USD");
        }

        @Test
        void shouldSerializeAndDeserializeAccountSuspended() throws Exception {
            AccountSuspendedProblemDetail original = new AccountSuspendedProblemDetail(
                    "Your account has been suspended",
                    "Violation of terms of service"
            );

            String json = objectMapper.writeValueAsString(original);
            AccountSuspendedProblemDetail deserialized = objectMapper.readValue(json, AccountSuspendedProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
            assertThat(deserialized.getTitle()).isEqualTo("Account Suspended");
            assertThat(deserialized.getStatus()).isEqualTo(422);
            assertThat(deserialized.getDetail()).isEqualTo("Your account has been suspended");
            assertThat(deserialized.getCode()).isEqualTo("ACCOUNT_SUSPENDED");
            assertThat(deserialized.getAttributes().reason()).isEqualTo("Violation of terms of service");
        }
    }

    @Nested
    class ValidationProblemDetailTest {

        @Test
        void shouldSerializeAndDeserializeInvalidFormatErrors() throws Exception {
            ValidationProblemDetail original = new ValidationProblemDetail()
                    .addInvalidFormat("Email must be a valid email address", "email", "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
                    .addInvalidFormat("Phone must match pattern", "phone", "^\\+?[1-9]\\d{1,14}$");

            String json = objectMapper.writeValueAsString(original);
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
            assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
            assertThat(deserialized.getStatus()).isEqualTo(400);
            assertThat(deserialized.getErrors()).hasSize(2);

            InvalidFormatValidationError error = (InvalidFormatValidationError) deserialized.getErrors().get(0);
            assertThat(error.getCode()).isEqualTo("invalid_format");
            assertThat(error.getDetail()).isEqualTo("Email must be a valid email address");
            assertThat(error.getRef()).isEqualTo("email");
            assertThat(error.getAttributes().pattern()).isEqualTo("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
        }

        @Test
        void shouldSerializeAndDeserializeMissingValueErrors() throws Exception {
            ValidationProblemDetail original = new ValidationProblemDetail()
                    .addMissingValue("Name is required", "name", "name")
                    .addMissingValue("Email is required", "email", "email");

            String json = objectMapper.writeValueAsString(original);
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
            assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
            assertThat(deserialized.getStatus()).isEqualTo(400);
            assertThat(deserialized.getErrors()).hasSize(2);

            MissingValueValidationError error = (MissingValueValidationError) deserialized.getErrors().get(0);
            assertThat(error.getCode()).isEqualTo("missing_value");
            assertThat(error.getDetail()).isEqualTo("Name is required");
            assertThat(error.getRef()).isEqualTo("name");
            assertThat(error.getAttributes().missingField()).isEqualTo("name");
        }
    }

    @Nested
    class RoundTripTest {

        @Test
        void shouldRoundTripTransferLimitExceededProblemDetail() throws Exception {
            TransferLimitExceededProblemDetail original = new TransferLimitExceededProblemDetail(
                    "Transfer amount exceeds your daily limit",
                    new BigDecimal("15000.00"),
                    "USD"
            );

            String json = objectMapper.writeValueAsString(original);
            TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json, TransferLimitExceededProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
            assertThat(deserialized.getDetail()).isEqualTo(original.getDetail());
            assertThat(deserialized.getCode()).isEqualTo(original.getCode());
            assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo(original.getAttributes().amount());
            assertThat(deserialized.getAttributes().currency()).isEqualTo(original.getAttributes().currency());
        }

        @Test
        void shouldRoundTripValidationProblemDetail() throws Exception {
            ValidationProblemDetail original = ValidationProblemDetail.invalidFormat("Email must be valid", "email", "^[a-z]+@[a-z]+$");

            String json = objectMapper.writeValueAsString(original);
            ValidationProblemDetail deserialized = objectMapper.readValue(json, ValidationProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
            assertThat(deserialized.getErrors()).hasSize(original.getErrors().size());
            assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo(original.getErrors().get(0).getCode());
            assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo(original.getErrors().get(0).getDetail());
            assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo(original.getErrors().get(0).getRef());
        }
    }
}
