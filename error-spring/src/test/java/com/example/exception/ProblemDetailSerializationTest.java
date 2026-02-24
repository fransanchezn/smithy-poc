package com.example.exception;

import com.example.exception.access.AccessProblemDetail;
import com.example.exception.domain.AccountSuspendedProblemDetail;
import com.example.exception.domain.TransferLimitExceededProblemDetail;
import com.example.exception.server.ServerProblemDetail;
import com.example.exception.validation.InvalidFormatProblemDetail;
import com.example.exception.validation.MissingValueProblemDetail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

import java.math.BigDecimal;

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
        void shouldSerializeUnauthorized() throws Exception {
            AccessProblemDetail problemDetail = AccessProblemDetail.unauthorized("Invalid credentials");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/access-error");
            assertThat(node.get("title").asText()).isEqualTo("Unauthorized");
            assertThat(node.get("status").asInt()).isEqualTo(401);
            assertThat(node.get("detail").asText()).isEqualTo("Invalid credentials");
        }

        @Test
        void shouldSerializeForbidden() throws Exception {
            AccessProblemDetail problemDetail = AccessProblemDetail.forbidden("Access denied");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/access-error");
            assertThat(node.get("title").asText()).isEqualTo("Forbidden");
            assertThat(node.get("status").asInt()).isEqualTo(403);
            assertThat(node.get("detail").asText()).isEqualTo("Access denied");
        }

        @Test
        void shouldDeserializeToBaseProblemDetail() throws Exception {
            String json = """
                {
                    "type": "https://errors.example.com/access-error",
                    "title": "Unauthorized",
                    "status": 401,
                    "detail": "Invalid credentials"
                }
                """;

            ProblemDetail problemDetail = objectMapper.readValue(json, ProblemDetail.class);

            assertThat(problemDetail.getType().toString()).isEqualTo("https://errors.example.com/access-error");
            assertThat(problemDetail.getTitle()).isEqualTo("Unauthorized");
            assertThat(problemDetail.getStatus()).isEqualTo(401);
            assertThat(problemDetail.getDetail()).isEqualTo("Invalid credentials");
        }
    }

    @Nested
    class ServerProblemDetailTest {

        @Test
        void shouldSerializeInternalServerError() throws Exception {
            ServerProblemDetail problemDetail = ServerProblemDetail.internalServerError("Something went wrong");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/server-error");
            assertThat(node.get("title").asText()).isEqualTo("Internal Server Error");
            assertThat(node.get("status").asInt()).isEqualTo(500);
            assertThat(node.get("detail").asText()).isEqualTo("Something went wrong");
        }

        @Test
        void shouldSerializeServiceUnavailable() throws Exception {
            ServerProblemDetail problemDetail = ServerProblemDetail.serviceUnavailable("Service is down");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/server-error");
            assertThat(node.get("title").asText()).isEqualTo("Service Unavailable");
            assertThat(node.get("status").asInt()).isEqualTo(503);
            assertThat(node.get("detail").asText()).isEqualTo("Service is down");
        }
    }

    @Nested
    class DomainProblemDetailTest {

        @Test
        void shouldSerializeTransferLimitExceeded() throws Exception {
            TransferLimitExceededProblemDetail problemDetail = new TransferLimitExceededProblemDetail(
                    "Transfer amount exceeds your daily limit",
                    new BigDecimal("15000.00"),
                    "USD"
            );

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/domain-error");
            assertThat(node.get("title").asText()).isEqualTo("Transfer Limit Exceeded");
            assertThat(node.get("status").asInt()).isEqualTo(422);
            assertThat(node.get("detail").asText()).isEqualTo("Transfer amount exceeds your daily limit");
            assertThat(node.get("code").asText()).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(node.get("attributes").get("amount").decimalValue()).isEqualByComparingTo("15000.00");
            assertThat(node.get("attributes").get("currency").asText()).isEqualTo("USD");
        }

        @Test
        void shouldSerializeAccountSuspended() throws Exception {
            AccountSuspendedProblemDetail problemDetail = new AccountSuspendedProblemDetail(
                    "Your account has been suspended",
                    "Violation of terms of service"
            );

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("https://errors.example.com/domain-error");
            assertThat(node.get("title").asText()).isEqualTo("Account Suspended");
            assertThat(node.get("status").asInt()).isEqualTo(422);
            assertThat(node.get("detail").asText()).isEqualTo("Your account has been suspended");
            assertThat(node.get("code").asText()).isEqualTo("ACCOUNT_SUSPENDED");
            assertThat(node.get("attributes").get("reason").asText()).isEqualTo("Violation of terms of service");
        }

        @Test
        void shouldDeserializeDomainErrorWithAttributes() throws Exception {
            String json = """
                {
                    "type": "https://errors.example.com/domain-error",
                    "title": "Transfer Limit Exceeded",
                    "status": 422,
                    "detail": "Transfer amount exceeds your daily limit",
                    "code": "TRANSFER_LIMIT_EXCEEDED",
                    "attributes": {
                        "amount": 15000.00,
                        "currency": "USD"
                    }
                }
                """;

            ProblemDetail problemDetail = objectMapper.readValue(json, ProblemDetail.class);

            assertThat(problemDetail.getType().toString()).isEqualTo("https://errors.example.com/domain-error");
            assertThat(problemDetail.getTitle()).isEqualTo("Transfer Limit Exceeded");
            assertThat(problemDetail.getStatus()).isEqualTo(422);
            assertThat(problemDetail.getDetail()).isEqualTo("Transfer amount exceeds your daily limit");
            assertThat(problemDetail.getProperties()).containsKey("code");
            assertThat(problemDetail.getProperties().get("code")).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
            assertThat(problemDetail.getProperties()).containsKey("attributes");
        }
    }

    @Nested
    class ValidationProblemDetailTest {

        @Test
        void shouldSerializeInvalidFormatErrors() throws Exception {
            InvalidFormatProblemDetail problemDetail = new InvalidFormatProblemDetail();
            problemDetail.addError("Email must be a valid email address", "email", "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
            problemDetail.addError("Phone must match pattern", "phone", "^\\+?[1-9]\\d{1,14}$");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("/errors/types/validation");
            assertThat(node.get("title").asText()).isEqualTo("Validation Problem");
            assertThat(node.get("status").asInt()).isEqualTo(400);
            assertThat(node.get("errors")).isNotNull();
            assertThat(node.get("errors").size()).isEqualTo(2);

            JsonNode firstError = node.get("errors").get(0);
            assertThat(firstError.get("code").asText()).isEqualTo("invalid_format");
            assertThat(firstError.get("detail").asText()).isEqualTo("Email must be a valid email address");
            assertThat(firstError.get("ref").asText()).isEqualTo("email");
            assertThat(firstError.get("attributes").get("pattern").asText()).isEqualTo("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
        }

        @Test
        void shouldSerializeMissingValueErrors() throws Exception {
            MissingValueProblemDetail problemDetail = new MissingValueProblemDetail();
            problemDetail.addError("Name is required", "name", "name");
            problemDetail.addError("Email is required", "email", "email");

            String json = objectMapper.writeValueAsString(problemDetail);
            JsonNode node = objectMapper.readTree(json);

            assertThat(node.get("type").asText()).isEqualTo("/errors/types/validation");
            assertThat(node.get("title").asText()).isEqualTo("Validation Problem");
            assertThat(node.get("status").asInt()).isEqualTo(400);
            assertThat(node.get("errors")).isNotNull();
            assertThat(node.get("errors").size()).isEqualTo(2);

            JsonNode firstError = node.get("errors").get(0);
            assertThat(firstError.get("code").asText()).isEqualTo("missing_value");
            assertThat(firstError.get("detail").asText()).isEqualTo("Name is required");
            assertThat(firstError.get("ref").asText()).isEqualTo("name");
            assertThat(firstError.get("attributes").get("missingField").asText()).isEqualTo("name");
        }

        @Test
        void shouldDeserializeValidationErrors() throws Exception {
            String json = """
                {
                    "type": "/errors/types/validation",
                    "title": "Validation Problem",
                    "status": 400,
                    "errors": [
                        {
                            "code": "invalid_format",
                            "detail": "Email must be valid",
                            "ref": "email",
                            "attributes": {
                                "pattern": "^[a-z]+@[a-z]+$"
                            }
                        }
                    ]
                }
                """;

            ProblemDetail problemDetail = objectMapper.readValue(json, ProblemDetail.class);

            assertThat(problemDetail.getType().toString()).isEqualTo("/errors/types/validation");
            assertThat(problemDetail.getTitle()).isEqualTo("Validation Problem");
            assertThat(problemDetail.getStatus()).isEqualTo(400);
            assertThat(problemDetail.getProperties()).containsKey("errors");
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
            ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
            assertThat(deserialized.getDetail()).isEqualTo(original.getDetail());
            assertThat(deserialized.getProperties().get("code")).isEqualTo("TRANSFER_LIMIT_EXCEEDED");
        }

        @Test
        void shouldRoundTripValidationProblemDetail() throws Exception {
            InvalidFormatProblemDetail original = new InvalidFormatProblemDetail();
            original.addError("Email must be valid", "email", "^[a-z]+@[a-z]+$");

            String json = objectMapper.writeValueAsString(original);
            ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

            assertThat(deserialized.getType()).isEqualTo(original.getType());
            assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
            assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
            assertThat(deserialized.getProperties()).containsKey("errors");
        }
    }
}
