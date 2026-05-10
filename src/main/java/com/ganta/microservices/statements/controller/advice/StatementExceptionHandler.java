package com.ganta.microservices.statements.controller.advice;

import com.ganta.microservices.statements.exception.StatementErrorCode;
import com.ganta.microservices.statements.exception.StatementErrorResponse;
import com.ganta.microservices.statements.exception.StatementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class StatementExceptionHandler {

    @ExceptionHandler(StatementException.class)
    public ResponseEntity<StatementErrorResponse> handleStatementException(StatementException exception) {
        StatementErrorCode errorCode = exception.getErrorCode();

        StatementErrorResponse response = StatementErrorResponse.builder()
                .code(errorCode.name())
                .message(exception.getMessage())
                .status(errorCode.getHttpStatus().value())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StatementErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
        StatementErrorResponse response = StatementErrorResponse.builder()
                .code("MISSING_REQUEST_PARAMETER")
                .message("Required request parameter is missing: " + exception.getParameterName())
                .status(400)
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StatementErrorResponse> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error occurred", exception);

        StatementErrorCode errorCode = StatementErrorCode.INTERNAL_SERVER_ERROR;

        StatementErrorResponse response = StatementErrorResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}
