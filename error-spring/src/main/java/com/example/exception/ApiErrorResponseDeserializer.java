package com.example.exception;

import com.example.exception.access.InvalidTokenAccessErrorResponseException;
import com.example.exception.domain.AccountSuspendedException;
import com.example.exception.domain.TransferLimitExceededException;
import com.example.exception.server.ServerErrorResponseException;
import com.example.exception.validation.ValidationErrorResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Deserializer for API error responses. Uses the x-error-type header to determine which exception
 * class to deserialize into, enabling type-safe access to exception properties.
 */
public class ApiErrorResponseDeserializer {

  private static final Map<String, Class<? extends ApiErrorResponseException>> EXCEPTION_TYPES =
      Map.of(
          "TransferLimitExceededException", TransferLimitExceededException.class,
          "AccountSuspendedException", AccountSuspendedException.class,
          "ValidationErrorResponseException", ValidationErrorResponseException.class,
          "InvalidTokenAccessErrorResponseException", InvalidTokenAccessErrorResponseException.class,
          "ServerErrorResponseException", ServerErrorResponseException.class
      );

  private final ObjectMapper objectMapper;

  public ApiErrorResponseDeserializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Deserializes a JSON error response into the appropriate exception type based on the error type
   * header.
   *
   * @param json            the JSON response body
   * @param errorTypeHeader the value of the x-error-type header
   * @param <T>             the expected exception type
   * @return the deserialized exception with full type safety
   * @throws JacksonException    if JSON parsing fails
   * @throws IllegalArgumentException   if the error type is unknown
   */
  @SuppressWarnings("unchecked")
  public <T extends ApiErrorResponseException> T deserialize(String json, String errorTypeHeader)
      throws JacksonException {
    Class<? extends ApiErrorResponseException> targetClass = EXCEPTION_TYPES.get(errorTypeHeader);
    if (targetClass == null) {
      throw new IllegalArgumentException("Unknown error type: " + errorTypeHeader);
    }
    return (T) objectMapper.readValue(json, targetClass);
  }

  /**
   * Deserializes a JSON error response into the specified exception class.
   *
   * @param json        the JSON response body
   * @param targetClass the exception class to deserialize into
   * @param <T>         the exception type
   * @return the deserialized exception
   * @throws JacksonException if JSON parsing fails
   */
  public <T extends ApiErrorResponseException> T deserialize(String json, Class<T> targetClass)
      throws JacksonException {
    return objectMapper.readValue(json, targetClass);
  }

  /**
   * Gets the exception class for a given error type header value.
   *
   * @param errorTypeHeader the value of the x-error-type header
   * @return the corresponding exception class, or null if unknown
   */
  public Class<? extends ApiErrorResponseException> getExceptionClass(String errorTypeHeader) {
    return EXCEPTION_TYPES.get(errorTypeHeader);
  }
}
