package com.example.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.exception.access.AccessErrorCode;
import com.example.exception.access.InvalidTokenAccessErrorResponseException;
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
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.json.ProblemDetailJacksonMixin;

class ExceptionSerializationTest {

  private ObjectMapper objectMapper;
  private ApiErrorResponseDeserializer deserializer;

  @BeforeEach
  void setUp() {
    objectMapper = JsonMapper.builder()
        .addMixIn(ProblemDetail.class, ProblemDetailJacksonMixin.class)
        .build();
    deserializer = new ApiErrorResponseDeserializer(objectMapper);
  }

  @Nested
  class AccessExceptionTest {

    @Test
    void shouldSerializeAndDeserializeAccessException() {
      InvalidTokenAccessErrorResponseException original = InvalidTokenAccessErrorResponseException.builder()
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      InvalidTokenAccessErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/access"));
      assertThat(deserialized.getTitle()).isEqualTo("Access Error");
      assertThat(deserialized.getStatus()).isEqualTo(401);
      assertThat(deserialized.getDetail()).isEqualTo("Invalid Token");
      assertThat(deserialized.getCode()).isEqualTo(AccessErrorCode.UNAUTHORIZED);
    }

    @Test
    void shouldHaveCorrectStatusCode() {
      InvalidTokenAccessErrorResponseException exception = InvalidTokenAccessErrorResponseException.builder()
          .build();

      assertThat(exception.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      InvalidTokenAccessErrorResponseException exception = InvalidTokenAccessErrorResponseException.builder()
          .build();

      assertThat(exception.getHeaders().getFirst("x-error-type"))
          .isEqualTo("InvalidTokenAccessErrorResponseException");
    }
  }

  @Nested
  class ServerExceptionTest {

    @Test
    void shouldSerializeAndDeserializeServerErrorException() {
      ServerErrorResponseException original = ServerErrorResponseException.builder()
          .title("Internal Server Error")
          .detail("Database connection failed")
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ServerErrorResponseException deserialized = deserializer.deserialize(json, errorType);

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
    void shouldSerializeAndDeserializeTransferLimitExceededException() {
      TransferLimitExceededException original = TransferLimitExceededException.builder()
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("50000.00"))
              .currency("EUR")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      TransferLimitExceededException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Transfer Limit Exceeded");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Transfer Limit has been exceeded");
      assertThat(deserialized.getCode().getCode()).isEqualTo("transfer.transfer_limit_exceeded");
      assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo("50000.00");
      assertThat(deserialized.getAttributes().currency()).isEqualTo("EUR");
    }

    @Test
    void shouldSerializeAndDeserializeAccountSuspendedException() {
      AccountSuspendedException original = AccountSuspendedException.builder()
          .attributes(AccountSuspendedAttributes.builder()
              .reason("Multiple failed login attempts")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      AccountSuspendedException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/domain"));
      assertThat(deserialized.getTitle()).isEqualTo("Account Suspended");
      assertThat(deserialized.getStatus()).isEqualTo(422);
      assertThat(deserialized.getDetail()).isEqualTo("Account has been suspended");
      assertThat(deserialized.getCode().getCode()).isEqualTo("account.account_suspended");
      assertThat(deserialized.getAttributes().reason()).isEqualTo("Multiple failed login attempts");
    }

    @Test
    void shouldProvideTypedAccessToAttributes() {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("1000"))
              .currency("USD")
              .build())
          .build();

      assertThat(exception.getCode().getCode()).isEqualTo("transfer.transfer_limit_exceeded");
      assertThat(exception.getAttributes().amount()).isEqualByComparingTo("1000");
      assertThat(exception.getAttributes().currency()).isEqualTo("USD");
    }

    @Test
    void shouldHaveErrorTypeHeader() {
      TransferLimitExceededException exception = TransferLimitExceededException.builder()
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
    void shouldSerializeAndDeserializeInvalidFormatErrors() {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
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

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ValidationErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
      assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
      assertThat(deserialized.getStatus()).isEqualTo(400);
      assertThat(deserialized.getErrors()).hasSize(2);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("Email format is invalid");
      assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo("user.email");
      assertThat(((InvalidFormatValidationError) deserialized.getErrors().get(0)).getAttributes().pattern())
          .isEqualTo("^[\\w.-]+@[\\w.-]+\\.\\w+$");
      assertThat(deserialized.getErrors().get(1).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(1).getDetail()).isEqualTo("Date format is invalid");
      assertThat(deserialized.getErrors().get(1).getRef()).isEqualTo("user.birthDate");
    }

    @Test
    void shouldSerializeAndDeserializeMissingValueErrors() {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
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

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ValidationErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(URI.create("/errors/types/validation"));
      assertThat(deserialized.getTitle()).isEqualTo("Validation Problem");
      assertThat(deserialized.getStatus()).isEqualTo(400);
      assertThat(deserialized.getErrors()).hasSize(2);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("missing_value");
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("First name is required");
      assertThat(((MissingValueValidationError) deserialized.getErrors().get(0)).getAttributes().missingField())
          .isEqualTo("firstName");
      assertThat(deserialized.getErrors().get(1).getCode()).isEqualTo("missing_value");
      assertThat(deserialized.getErrors().get(1).getDetail()).isEqualTo("Last name is required");
    }

    @Test
    void shouldSerializeAndDeserializeMixedErrors() {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
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

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ValidationErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getErrors()).hasSize(2);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(1).getCode()).isEqualTo("missing_value");
    }

    @Test
    void shouldProvideTypedAccessToErrorsAfterDeserialization() {
      ValidationErrorResponseException original = ValidationErrorResponseException.builder()
          .error(InvalidFormatValidationError.builder()
              .detail("Invalid email")
              .ref("email")
              .attributes(InvalidFormatAttributes.builder()
                  .pattern("^[a-z]+@[a-z]+$")
                  .build())
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ValidationErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getErrors()).hasSize(1);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("Invalid email");
      assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo("email");
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
    void shouldRoundTripDomainException() {
      TransferLimitExceededException original = TransferLimitExceededException.builder()
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("25000"))
              .currency("GBP")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      TransferLimitExceededException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(original.getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
      assertThat(deserialized.getDetail()).isEqualTo(original.getDetail());
      assertThat(deserialized.getCode()).isEqualTo(original.getCode());
      assertThat(deserialized.getAttributes().amount()).isEqualByComparingTo(original.getAttributes().amount());
      assertThat(deserialized.getAttributes().currency()).isEqualTo(original.getAttributes().currency());
    }

    @Test
    void shouldRoundTripValidationException() {
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
      String errorType = original.getHeaders().getFirst("x-error-type");
      ValidationErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(original.getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
      assertThat(deserialized.getErrors()).hasSize(1);
      assertThat(deserialized.getErrors().get(0).getCode()).isEqualTo("invalid_format");
      assertThat(deserialized.getErrors().get(0).getDetail()).isEqualTo("Invalid format");
      assertThat(deserialized.getErrors().get(0).getRef()).isEqualTo("field");
      assertThat(((InvalidFormatValidationError) deserialized.getErrors().get(0)).getAttributes().pattern())
          .isEqualTo("pattern");
    }

    @Test
    void shouldRoundTripAccessException() {
      InvalidTokenAccessErrorResponseException original = InvalidTokenAccessErrorResponseException.builder()
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      InvalidTokenAccessErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(original.getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
      assertThat(deserialized.getDetail()).isEqualTo(original.getDetail());
      assertThat(deserialized.getCode()).isEqualTo(original.getCode());
    }

    @Test
    void shouldRoundTripServerException() {
      ServerErrorResponseException original = ServerErrorResponseException.builder()
          .title("Service Unavailable")
          .detail("Please try again later")
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());
      String errorType = original.getHeaders().getFirst("x-error-type");
      ServerErrorResponseException deserialized = deserializer.deserialize(json, errorType);

      assertThat(deserialized.getType()).isEqualTo(original.getType());
      assertThat(deserialized.getTitle()).isEqualTo(original.getTitle());
      assertThat(deserialized.getStatus()).isEqualTo(original.getStatus());
      assertThat(deserialized.getDetail()).isEqualTo(original.getDetail());
    }
  }

  @Nested
  class DeserializerApiTest {

    @Test
    void shouldDeserializeDirectlyToSpecificClass() {
      TransferLimitExceededException original = TransferLimitExceededException.builder()
          .attributes(TransferLimitExceededAttributes.builder()
              .amount(new BigDecimal("1000"))
              .currency("USD")
              .build())
          .build();

      String json = objectMapper.writeValueAsString(original.getBody());

      TransferLimitExceededException deserialized =
          deserializer.deserialize(json, TransferLimitExceededException.class);

      assertThat(deserialized.getCode()).isEqualTo(original.getCode());
      assertThat(deserialized.getAttributes().amount())
          .isEqualByComparingTo(original.getAttributes().amount());
    }

    @Test
    void shouldThrowForUnknownErrorType() {
      String json = "{}";
      assertThatThrownBy(() -> deserializer.deserialize(json, "UnknownException"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Unknown error type: UnknownException");
    }

    @Test
    void shouldReturnCorrectExceptionClass() {
      assertThat(deserializer.getExceptionClass("TransferLimitExceededException"))
          .isEqualTo(TransferLimitExceededException.class);
      assertThat(deserializer.getExceptionClass("ValidationErrorResponseException"))
          .isEqualTo(ValidationErrorResponseException.class);
      assertThat(deserializer.getExceptionClass("UnknownException")).isNull();
    }
  }
}
