package com.ganta.microservices.statements.pojo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class GenerateStatementResponse {
    private Long accountNumber;
    private String message;
}
