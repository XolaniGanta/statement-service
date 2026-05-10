package com.ganta.microservices.statements.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItems {
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String reference;

    public enum TransactionType {
        DEBIT,
        CREDIT
    }
}
