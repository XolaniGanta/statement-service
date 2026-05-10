package com.ganta.microservices.statements.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StatementErrorCode {
    LINK_EXPIRED(HttpStatus.UNAUTHORIZED, "Statement download link has expired"),
    INVALID_DOWNLOAD_LINK(HttpStatus.UNAUTHORIZED, "Statement download link is invalid"),
    STATEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Statement not found"),
    STATEMENT_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Statement is not available for this period"),
    STATEMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Statement already exists for this period"),
    TRANSACTIONS_NOT_FOUND(HttpStatus.NOT_FOUND, "Transactions are not available for this account and period"),
    TRANSACTIONS_LOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load transactions"),
    STATEMENT_PDF_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Statement PDF file is not available"),
    STATEMENT_PDF_STORAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store generated statement PDF"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String message;

    StatementErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
