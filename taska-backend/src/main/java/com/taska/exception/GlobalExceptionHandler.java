package com.taska.exception;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles {@link ResourceNotFoundException} and returns a 404 Problem Detail response.
   *
   * @param exception the exception containing the not-found message
   * @return a 404 Problem Detail with the exception message as detail
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(ResourceNotFoundException exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }

  /**
   * Handles Bean Validation failures and returns a 400 Problem Detail response with a map of field
   * names to their validation error messages.
   *
   * @param exception the validation exception produced by {@code @Valid} constraints
   * @return a 400 Problem Detail with an {@code errors} property containing per-field messages
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors =
        exception.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    fieldError ->
                        fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "invalid"));
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    problemDetail.setProperty("errors", errors);
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }

  /**
   * Returns a client error when JSON cannot be deserialized, such as an Instant without an ISO-8601
   * timezone offset.
   *
   * @param exception the request-body deserialization failure
   * @return a 400 Problem Detail without exposing parser internals
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleUnreadableMessage(HttpMessageNotReadableException exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request body");
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }

  /** Returns a client error when a path or query parameter cannot be converted. */
  @ExceptionHandler(TypeMismatchException.class)
  public ProblemDetail handleTypeMismatch(TypeMismatchException exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameter");
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }

  /**
   * Catch-all handler for unexpected exceptions. Logs the full stack trace and returns a 500
   * Problem Detail to avoid leaking internal error details to clients.
   *
   * @param exception the unhandled exception
   * @return a 500 Problem Detail with a generic "Internal error" message
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception exception) {
    log.error("Unexpected error", exception);
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
    problemDetail.setType(URI.create("about:blank"));
    return problemDetail;
  }
}
