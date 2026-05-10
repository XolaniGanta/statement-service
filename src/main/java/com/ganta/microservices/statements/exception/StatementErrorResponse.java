package com.ganta.microservices.statements.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatementErrorResponse {
    private String code;
    private String message;
    private int status;
}
