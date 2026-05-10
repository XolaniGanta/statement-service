package com.ganta.microservices.statements.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerateStatementRequest {
    private Long accountNumber;
    private LocalDate startDate;
    private LocalDate endDate;

}
