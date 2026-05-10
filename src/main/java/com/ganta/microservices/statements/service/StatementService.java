package com.ganta.microservices.statements.service;

import com.ganta.microservices.statements.model.Statement;
import com.ganta.microservices.statements.pojo.GenerateStatementRequest;
import com.ganta.microservices.statements.pojo.GenerateStatementResponse;
import com.ganta.microservices.statements.pojo.StatementDownloadDto;
import org.springframework.core.io.Resource;

import java.time.LocalDate;
import java.util.UUID;

public interface StatementService {
    GenerateStatementResponse generateStatement(GenerateStatementRequest request);
    StatementDownloadDto downloadStatement(Long accountNumber, LocalDate periodStart, LocalDate periodEnd);
    Resource getStatementPdf(UUID statementId, String token);
    void generateMonthlyStatements();
}
