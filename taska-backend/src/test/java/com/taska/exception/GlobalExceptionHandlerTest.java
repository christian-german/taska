package com.taska.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

  @Test
  void unreadableRequestBodyReturnsBadRequest() {
    ProblemDetail problemDetail =
        globalExceptionHandler.handleUnreadableMessage(
            new HttpMessageNotReadableException(
                "Invalid Instant", new MockHttpInputMessage(new byte[0])));

    assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request body");
  }

  @Test
  void invalidPathParameterReturnsBadRequest() {
    ProblemDetail problemDetail =
        globalExceptionHandler.handleTypeMismatch(
            new TypeMismatchException("7541-09-08T07:46:56-19:07", Instant.class));

    assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problemDetail.getDetail()).isEqualTo("Invalid request parameter");
  }
}
