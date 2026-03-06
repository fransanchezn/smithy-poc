package com.example.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.exception.access.AccessErrorResponseException;
import com.example.exception.domain.AccountSuspendedAttributes;
import com.example.exception.domain.AccountSuspendedException;
import com.example.exception.domain.TransferLimitExceededAttributes;
import com.example.exception.domain.TransferLimitExceededException;
import com.example.exception.server.ServerErrorResponseException;
import com.example.exception.validation.InvalidFormatAttributes;
import com.example.exception.validation.InvalidFormatValidationError;
import com.example.exception.validation.MissingValueAttributes;
import com.example.exception.validation.MissingValueValidationError;
import com.example.exception.validation.ValidationErrorResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
    void shouldSerializeAccessException() throws Exception {
      AccessErrorResponseException exception = AccessErrorResponseException.builder()
          .title("Unauthorized")
          .detail("Invalid token")
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
      assertThat(deserialized.getTitle()).isEqualTo("Unauthorized");
      assertThat(deserialized.getStatus()).isEqualTo(401);
      assertThat(deserialized.getDetail()).isEqualTo("Invalid token");
    }

    @Test
    void shouldHaveCorrectStatusCode() {
      AccessErrorResponseException exception = AccessErrorResponseException.builder()
          .title("Unauthorized")
          .detail("test")
          .build();

      assertThat(exception.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      AccessErrorResponseException exception = AccessErrorResponseException.builder()
          .title("Unauthorized")
          .detail("test")
          .build();

      assertThat(exception.getHeaders().getFirst("x-error-type"))
          .isEqualTo("AccessErrorResponseException");
    }
  }

  @Nested
  class ServerExceptionTest {

    @Test
    void shouldSerializeServerErrorException() throws Exception {
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .title("Internal Server Error")
          .detail("Database connection failed")
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/server"));
      assertThat(deserialized.getTitle()).isEqualTo("Internal Server Error");
      assertThat(deserialized.getStatus()).isEqualTo(500);
      assertThat(deserialized.getDetail()).isEqualTo("Database connection failed");
    }

    @Test
    void shouldHaveCorrectStatusCode() {
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .title("Internal Server Error")
          .detail("test")
          .build();

      assertThat(exception.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      ServerErrorResponseException exception = ServerErrorResponseException.builder()
          .title("Internal Server Error")
          .detail("test")
          .build();

      assertThat(exception.getHeaders().getFirst("x-error-type"))
          .isEqualTo("ServerErrorResponseException");
    }
  }

  @Nested
  class DomainExceptionTest {

    @Test
    void shouldSerializeTransferLimitExceededException() throws Exception {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .detail("Your transfer exceeds the daily limit")
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("50000.00"))
              .currency("EUR")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Transfer Limit Exceeded");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Your transfer exceeds the daily limit");
      assertThat(deserialized.getProperties().get("code")).isEqualTo("transfer.transfer_limit_exceeded");
    }

    @Test
    void shouldSerializeAccountSuspendedException() throws Exception {
      AccountSuspendedException exception = AccountSuspendedException.builder()
          .detail("Account access denied")
          .attributes(AccountSuspendedAttributes.builder()
              .reason("Multiple failed login attempts")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Account Suspended");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Account access denied");
      assertThat(deserialized.getProperties().get("code")).isEqualTo("account.account_suspended");
    }

    @Test
    void shouldProvideTypedAccessToAttributes() {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .detail("Test")
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("1000"))
              .currency("USD")
              .build())
          .build();

      assertThat(exception.getCode()).isEqualTo("transfer.transfer_limit_exceeded");
      assertThat(exception.getAttributes().amount()).isEqualByComparingTo("1000");
      assertThat(exception.getAttributes().currency()).isEqualTo("USD");
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .detail("Test")
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("1000"))
              .currency("USD")
              .build())
          .build();

      assertThat(exception.getHeaders().getFirst("x-error-type"))
          .isEqualTo("TransferLimitExceededException");
    }
  }

  @Nested
  class ValidationExceptionTest {

    @Test
    void shouldSerializeInvalidFormatErrors() throws Exception {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
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
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
      assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
      assertThat(deserialized.getStatus()).isEqualTo(400);
      assertThat(exception.getErrors()).hasSize(2);
      assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("Email format is invalid");
      assertThat(exception.getErrors().get(0).getRef()).isEqualTo("user.email");
    }

    @Test
    void shouldSerializeMissingValueErrors() throws Exception {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
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
          .build();

      String json = objectMapper.writeValueAsString(exception.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
      assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
      assertThat(deserialized.getStatus()).isEqualTo(400);
      assertThat(exception.getErrors()).hasSize(2);
      assertThat(exception.getErrors().get(0).getCode()).isEqualTo("missing_value");
      assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("First name is required");
    }

    @Test
    void shouldSerializeMixedErrors() throws Exception {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
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
          .build();

      assertThat(exception.getErrors()).hasSize(2);
      assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(exception.getErrors().get(1).getCode()).isEqualTo("missing_value");
    }

    @Test
    void shouldProvideTypedAccessToErrors() {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .error(InvalidFormatValidationError.builder()
              .detail("Invalid email")
              .ref("email")
              .attributes(InvalidFormatAttributes.builder()
                  .pattern("^[a-z]+@[a-z]+$")
                  .build())
              .build())
          .build();

      assertThat(exception.getErrors()).hasSize(1);
      assertThat(exception.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(exception.getErrors().get(0).getDetail()).isEqualTo("Invalid email");
      assertThat(exception.getErrors().get(0).getRef()).isEqualTo("email");
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      ValidationErrorResponseException exception = ValidationErrorResponseException.builder()
          .error(InvalidFormatValidationError.builder()
              .detail("Invalid email")
              .ref("email")
              .attributes(InvalidFormatAttributes.builder()
                  .pattern("^[a-z]+@[a-z]+$")
                  .build())
              .build())
          .build();

      assertThat(exception.getHeaders().getFirst("x-error-type"))
          .isEqualTo("ValidationErrorResponseException");
    }
  }

  @Nested
  class RoundTripTest {

    @Test
    void shouldRoundTripDomainException() throws Exception {
      TransferLimitExceededException original = TransferLimitExceededException.builder()
          .detail("Transfer limit exceeded")
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("25000"))
              .currency("GBP")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
      assertThat(deserialized.getDetail()).isEqualTo(original.getBody().getDetail());
      assertThat(deserialized.getProperties().get("code")).isEqualTo(original.getCode());
    }

    @Test
    void shouldRoundTripValidationException() throws Exception {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
          .error(InvalidFormatValidationError.builder()
              .detail("Invalid format")
              .ref("field")
              .attributes(InvalidFormatAttributes.builder()
                  .pattern("pattern")
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      ProblemDetail deserialized = objectMapper.readValue(json, ProblemDetail.class);

      assertThat(deserialized.getType()).isEqualTo(original.getBody().getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getBody().getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getBody().getStatus());
      assertThat(original.getErrors()).hasSize(1);
      assertThat(original.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(original.getErrors().get(0).getDetail()).isEqualTo("Invalid format");
      assertThat(original.getErrors().get(0).getRef()).isEqualTo("field");
    }
  }
}
