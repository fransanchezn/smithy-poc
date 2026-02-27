package com.example.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.exception.access.AccessErrorResponseException;
import com.example.exception.access.AccessProblemDetail;
import com.example.exception.domain.AccountSuspendedAttributes;
import com.example.exception.domain.AccountSuspendedException;
import com.example.exception.domain.AccountSuspendedProblemDetail;
import com.example.exception.domain.TransferLimitExceededAttributes;
import com.example.exception.domain.TransferLimitExceededException;
import com.example.exception.domain.TransferLimitExceededProblemDetail;
import com.example.exception.server.ServerErrorResponseException;
import com.example.exception.server.ServerProblemDetail;
import com.example.exception.validation.InvalidFormatAttributes;
import com.example.exception.validation.InvalidFormatValidationError;
import com.example.exception.validation.MissingValueAttributes;
import com.example.exception.validation.MissingValueValidationError;
import com.example.exception.validation.ValidationErrorResponseException;
import com.example.exception.validation.ValidationProblemDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

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
      AccessErrorResponseException exception = AccessErrorResponseException.builder()
          .problemDetail(AccessProblemDetail.builder()
              .status(HttpStatus.UNAUTHORIZED)
              .title("Unauthorized")
              .detail("Invalid token")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
      assertThat(deserialized.getTitle()).isEqualTo("Unauthorized");
      assertThat(deserialized.getStatus()).isEqualTo(401);
      assertThat(deserialized.getDetail()).isEqualTo("Invalid token");
    }

    @Test
    void shouldSerializeAndDeserializeForbiddenException() throws Exception {
      AccessErrorResponseException exception = AccessErrorResponseException.builder()
          .problemDetail(AccessProblemDetail.builder()
              .status(HttpStatus.FORBIDDEN)
              .title("Forbidden")
              .detail("Insufficient permissions")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      AccessProblemDetail deserialized = objectMapper.readValue(json, AccessProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
      assertThat(deserialized.getTitle()).isEqualTo("Forbidden");
      assertThat(deserialized.getStatus()).isEqualTo(403);
      assertThat(deserialized.getDetail()).isEqualTo("Insufficient permissions");
    }

    @Test
    void shouldHaveCorrectStatusCode() {
      AccessErrorResponseException unauthorized = AccessErrorResponseException.builder()
          .problemDetail(AccessProblemDetail.builder()
              .status(HttpStatus.UNAUTHORIZED)
              .title("Unauthorized")
              .detail("test")
              .build())
          .build();
      AccessErrorResponseException forbidden = AccessErrorResponseException.builder()
          .problemDetail(AccessProblemDetail.builder()
              .status(HttpStatus.FORBIDDEN)
              .title("Forbidden")
              .detail("test")
              .build())
          .build();

      assertThat(unauthorized.getStatusCode().value()).isEqualTo(401);
      assertThat(forbidden.getStatusCode().value()).isEqualTo(403);
    }
  }

  @Nested
  class ServerExceptionTest {

    @Test
    void shouldSerializeAndDeserializeInternalServerErrorException() throws Exception {
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .problemDetail(ServerProblemDetail.builder()
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .title("Internal Server Error")
              .detail("Database connection failed")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/server"));
      assertThat(deserialized.getTitle()).isEqualTo("Internal Server Error");
      assertThat(deserialized.getStatus()).isEqualTo(500);
      assertThat(deserialized.getDetail()).isEqualTo("Database connection failed");
    }

    @Test
    void shouldSerializeAndDeserializeServiceUnavailableException() throws Exception {
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .problemDetail(ServerProblemDetail.builder()
              .status(HttpStatus.SERVICE_UNAVAILABLE)
              .title("Service Unavailable")
              .detail("Service is under maintenance")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ServerProblemDetail deserialized = objectMapper.readValue(json, ServerProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/server"));
      assertThat(deserialized.getTitle()).isEqualTo("Service Unavailable");
      assertThat(deserialized.getStatus()).isEqualTo(503);
      assertThat(deserialized.getDetail()).isEqualTo("Service is under maintenance");
    }

    @Test
    void shouldPreserveCause() {
      RuntimeException cause = new RuntimeException("Original error");
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .problemDetail(ServerProblemDetail.builder()
              .status(HttpStatus.INTERNAL_SERVER_ERROR)
              .title("Internal Server Error")
              .detail("Wrapped error")
              .build())
          .cause(cause)
          .build();

      assertThat(exception.getCause()).isEqualTo(cause);
    }
  }

  @Nested
  class DomainExceptionTest {

    @Test
    void shouldSerializeAndDeserializeTransferLimitExceededException() throws Exception {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .problemDetail(TransferLimitExceededProblemDetail.builder()
              .detail("Your transfer exceeds the daily limit")
              .attributes(TransferLimitExceededAttributes.builder()
                  .amount(new BigDecimal("50000.00"))
                  .currency("EUR")
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json,
          TransferLimitExceededProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Transfer Limit Exceeded");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Your transfer exceeds the daily limit");
      assertThat(deserialized.getCode()).isEqualTo("transfer.transfer_limit_exceeded");
      assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo("50000.00");
      assertThat(deserialized.getAttributes().currency()).isEqualTo("EUR");
    }

    @Test
    void shouldSerializeAndDeserializeAccountSuspendedException() throws Exception {
      AccountSuspendedException exception = AccountSuspendedException.builder()
          .problemDetail(AccountSuspendedProblemDetail.builder()
              .detail("Account access denied")
              .attributes(AccountSuspendedAttributes.builder()
                  .reason("Multiple failed login attempts")
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      AccountSuspendedProblemDetail deserialized = objectMapper.readValue(json,
          AccountSuspendedProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Account Suspended");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Account access denied");
      assertThat(deserialized.getCode()).isEqualTo("account.account_suspended");
      assertThat(deserialized.getAttributes().reason()).isEqualTo("Multiple failed login attempts");
    }

    @Test
    void shouldProvideTypedAccessToAttributes() {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .problemDetail(TransferLimitExceededProblemDetail.builder()
              .detail("Test")
              .attributes(TransferLimitExceededAttributes.builder()
                  .amount(new BigDecimal("1000"))
                  .currency("USD")
                  .build())
              .build())
          .build();

      assertThat(exception.getCode()).isEqualTo("transfer.transfer_limit_exceeded");
      assertThat(exception.getAttributes().amount()).isEqualByComparingTo("1000");
      assertThat(exception.getAttributes().currency()).isEqualTo("USD");
    }
  }

  @Nested
  class ValidationExceptionTest {

    @Test
    void shouldSerializeAndDeserializeInvalidFormatErrors() throws Exception {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .problemDetail(ValidationProblemDetail.builder()
              .error(InvalidFormatValidationError.builder()
                  .detail("Email format is invalid")
                  .ref("user.email")
                  .attributes(InvalidFormatAttributes.builder()
                      .pattern("^[\\w.-]+@[\\w.-]+\\.\\w+$")
                      .build())
                  .build())
              .error(InvalidFormatValidationError.builder()
                  .detail("Date format is invalid")
                  .ref("user.birthDate")
                  .attributes(InvalidFormatAttributes.builder()
                      .pattern("^\\d{4}-\\d{2}-\\d{2}$")
                      .build())
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ValidationProblemDetail deserialized = objectMapper.readValue(json,
          ValidationProblemDetail.class);

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
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .problemDetail(ValidationProblemDetail.builder()
              .error(MissingValueValidationError.builder()
                  .detail("First name is required")
                  .ref("firstName")
                  .attributes(MissingValueAttributes.builder()
                      .missingField("firstName")
                      .build())
                  .build())
              .error(MissingValueValidationError.builder()
                  .detail("Last name is required")
                  .ref("lastName")
                  .attributes(MissingValueAttributes.builder()
                      .missingField("lastName")
                      .build())
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ValidationProblemDetail deserialized = objectMapper.readValue(json,
          ValidationProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
      assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
      assertThat(deserialized.getStatus()).isEqualTo(400);
      assertThat(deserialized.getErrors()).hasSize(2);

      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("missing_value");
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("First name is required");
    }

    @Test
    void shouldSerializeAndDeserializeMixedErrors() throws Exception {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .problemDetail(ValidationProblemDetail.builder()
              .error(InvalidFormatValidationError.builder()
                  .detail("Invalid email")
                  .ref("email")
                  .attributes(InvalidFormatAttributes.builder()
                      .pattern("^[a-z]+@[a-z]+$")
                      .build())
                  .build())
              .error(MissingValueValidationError.builder()
                  .detail("Name is required")
                  .ref("name")
                  .attributes(MissingValueAttributes.builder()
                      .missingField("name")
                      .build())
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ValidationProblemDetail deserialized = objectMapper.readValue(json,
          ValidationProblemDetail.class);

      assertThat(deserialized.getErrors()).hasSize(2);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(1).getCode()).isEqualTo("missing_value");
    }

    @Test
    void shouldProvideTypedAccessToErrors() {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .problemDetail(ValidationProblemDetail.builder()
              .error(InvalidFormatValidationError.builder()
                  .detail("Invalid email")
                  .ref("email")
                  .attributes(InvalidFormatAttributes.builder()
                      .pattern("^[a-z]+@[a-z]+$")
                      .build())
                  .build())
              .build())
          .build();

      assertThat(exception.getErrors()).hasSize(1);
      assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("Invalid email");
      assertThat(exception.getErrors().get(0).getRef()).isEqualTo("email");
    }
  }

  @Nested
  class RoundTripTest {

    @Test
    void shouldRoundTripDomainException() throws Exception {
      TransferLimitExceededException original = TransferLimitExceededException.builder()
          .problemDetail(TransferLimitExceededProblemDetail.builder()
              .detail("Transfer limit exceeded")
              .attributes(TransferLimitExceededAttributes.builder()
                  .amount(new BigDecimal("25000"))
                  .currency("GBP")
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      TransferLimitExceededProblemDetail deserialized = objectMapper.readValue(json,
          TransferLimitExceededProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
      assertThat(deserialized.getDetail()).isEqualTo(original.getBody().getDetail());
      assertThat(deserialized.getCode()).isEqualTo(original.getCode());
      assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo(
          original.getAttributes().amount());
      assertThat(deserialized.getAttributes().currency()).isEqualTo(
          original.getAttributes().currency());
    }

    @Test
    void shouldRoundTripValidationException() throws Exception {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
          .problemDetail(ValidationProblemDetail.builder()
              .error(InvalidFormatValidationError.builder()
                  .detail("Invalid format")
                  .ref("field")
                  .attributes(InvalidFormatAttributes.builder()
                      .pattern("pattern")
                      .build())
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      ValidationProblemDetail deserialized = objectMapper.readValue(json,
          ValidationProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
      assertThat(deserialized.getErrors()).hasSize(original.getErrors().size());
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo(
          original.getErrors().get(0).getCode());
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo(
          original.getErrors().get(0).getDetail());
      assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo(
          original.getErrors().get(0).getRef());
    }
  }
}
