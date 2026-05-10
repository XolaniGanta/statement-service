package com.ganta.microservices.statements.service.impl;


import com.ganta.microservices.statements.exception.StatementErrorCode;
import com.ganta.microservices.statements.exception.StatementException;
import com.ganta.microservices.statements.model.Statement;
import com.ganta.microservices.statements.pojo.GenerateStatementRequest;
import com.ganta.microservices.statements.pojo.GenerateStatementResponse;
import com.ganta.microservices.statements.pojo.StatementDownloadDto;
import com.ganta.microservices.statements.pojo.Transactions;
import com.ganta.microservices.statements.repository.StatementRepository;
import com.ganta.microservices.statements.service.StatementGenerateService;
import com.ganta.microservices.statements.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepository;
    private final StatementGenerateService statementGenerateService;
    private final TransactionServiceImpl transactionService;
    private final StatementDownloadJwtService statementDownloadJwtService;

    @Value("${statement.storage.location}")
    private String statementStorageLocation;

    @Override
    public GenerateStatementResponse generateStatement(GenerateStatementRequest request) {
        log.info("generateStatement - method entered - accountNumber: {}", request.getAccountNumber());

        Transactions transactions = transactionService.getTransactions(request.getAccountNumber(), request.getStartDate(), request.getEndDate());

        boolean statementExists = statementRepository.existsByAccountNumberAndPeriodStartAndPeriodEnd(request.getAccountNumber(), request.getStartDate(), request.getEndDate());

        if (statementExists) {
            throw new StatementException(StatementErrorCode.STATEMENT_ALREADY_EXISTS);
        }

        return generateStatementFromTransactions(transactions);
    }

    @Override
    public StatementDownloadDto downloadStatement(Long accountNumber, LocalDate periodStart, LocalDate periodEnd) {
        log.info("downloadStatement - method entered - accountNumber: {}, periodStart: {}, periodEnd: {}", accountNumber, periodStart, periodEnd);

        Statement statement = statementRepository.findByAccountNumberAndPeriodStartAndPeriodEnd(accountNumber, periodStart, periodEnd)
                .orElseThrow(() -> new StatementException(StatementErrorCode.STATEMENT_NOT_AVAILABLE));

        String token = statementDownloadJwtService.generateToken(statement.getStatementId());
        LocalDateTime expiresAt = LocalDateTime.ofInstant(
                statementDownloadJwtService.getTokenExpiry(token),
                ZoneId.systemDefault()
        );

        String downloadUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/statements/")
                .path(statement.getStatementId().toString())
                .path("/file")
                .queryParam("token", token)
                .toUriString();

        log.info("downloadStatement - method returned - statementId: {}", statement.getStatementId());
        return new StatementDownloadDto(statement.getStatementId(), downloadUrl, expiresAt);
    }

    @Override
    public Resource getStatementPdf(UUID statementId, String token) {
        log.info("getStatementPdf - method entered - statementId: {}", statementId);

        statementDownloadJwtService.validateForStatement(statementId, token);

        Statement statement = statementRepository.findByStatementId(statementId)
                .orElseThrow(() -> new StatementException(StatementErrorCode.STATEMENT_NOT_FOUND));

        FileSystemResource resource = new FileSystemResource(statement.getFilePath());

        if (!resource.exists()) {
            throw new StatementException(StatementErrorCode.STATEMENT_PDF_NOT_AVAILABLE);
        }

        return resource;
    }

    @Override
    public void generateMonthlyStatements() {
        log.info("generateMonthlyStatements - monthly statement batch started");

        List<Transactions> allTransactions = transactionService.getAllTransactions();

        for (Transactions transactions : allTransactions) {
            boolean statementExists = statementRepository.existsByAccountNumberAndPeriodStartAndPeriodEnd(
                    transactions.getAccountNumber(),
                    transactions.getPeriodStart(),
                    transactions.getPeriodEnd()
            );

            if (statementExists) {
                log.info(
                        "Statement already exists for accountNumber={}, periodStart={}, periodEnd={}",
                        transactions.getAccountNumber(),
                        transactions.getPeriodStart(),
                        transactions.getPeriodEnd()
                );
                continue;
            }

            generateStatementFromTransactions(transactions);
        }

        log.info("Monthly statement batch completed");
    }

    private GenerateStatementResponse generateStatementFromTransactions(Transactions transactions) {
        log.info("generateStatementFromTransactions - method entered");
        UUID statementId = UUID.randomUUID();

        byte[] pdfBytes = statementGenerateService.generatePdf(transactions);

        try {
            Path storageDirectory = Path.of(statementStorageLocation);
            Files.createDirectories(storageDirectory);

            Path pdfPath = storageDirectory.resolve(statementId + ".pdf");

            Files.write(pdfPath, pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Statement statement = buildStatement(transactions, pdfPath.toString(), statementId);

            statementRepository.save(statement);

            GenerateStatementResponse statementResponse = buildGenerateStatementResponse(transactions);

            log.info(
                    "Generated monthly statement for accountNumber={}, statementId={}, periodStart={}, periodEnd={}",
                    transactions.getAccountNumber(),
                    statementId,
                    transactions.getPeriodStart(),
                    transactions.getPeriodEnd()
            );

            return statementResponse;

        } catch (Exception e) {
            log.error(
                    "Failed to store monthly statement PDF for accountNumber={}, periodStart={}, periodEnd={}",
                    transactions.getAccountNumber(),
                    transactions.getPeriodStart(),
                    transactions.getPeriodEnd(),
                    e
            );
            throw new StatementException(StatementErrorCode.STATEMENT_PDF_STORAGE_FAILED, e);
        }
    }

    private static Statement buildStatement(Transactions transactions, String filePath, UUID statementId) {
        return Statement.builder()
                .statementId(statementId)
                .accountNumber(transactions.getAccountNumber())
                .periodStart(transactions.getPeriodStart())
                .periodEnd(transactions.getPeriodEnd())
                .filePath(filePath)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private static GenerateStatementResponse buildGenerateStatementResponse(Transactions transactions){
        return GenerateStatementResponse.builder()
                .accountNumber(transactions.getAccountNumber())
                .message("Statement generated successfully")
                .build();
    }

}

