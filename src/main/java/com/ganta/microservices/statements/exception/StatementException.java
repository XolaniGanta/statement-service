package com.ganta.microservices.statements.exception;

import lombok.Getter;

@Getter
public class StatementException extends RuntimeException{
    private final StatementErrorCode errorCode;

    public StatementException(StatementErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public StatementException(StatementErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public StatementException(StatementErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public StatementException(StatementErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
