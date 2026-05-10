package com.ganta.microservices.statements.service;


import com.ganta.microservices.statements.model.Statement;
import com.ganta.microservices.statements.pojo.GenerateStatementRequest;
import com.ganta.microservices.statements.pojo.StatementDownloadDto;
import com.ganta.microservices.statements.pojo.Transactions;
import com.ganta.microservices.statements.repository.StatementRepository;
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
    private final PdfGenerateService pdfGenerateService;
    private final TransactionServiceImpl transactionService;
    private final StatementDownloadJwtService statementDownloadJwtService;

    @Value("${statement.storage.location}")
    private String statementStorageLocation;

    @Override
    public Statement generateStatement(GenerateStatementRequest request) {

        Transactions transactions = transactionService.getTransactions(
                request.getAccountNumber(),
                request.getStartDate(),
                request.getEndDate()
        );

        boolean statementExists = statementRepository.existsByAccountNumberAndPeriodStartAndPeriodEnd(
                request.getAccountNumber(),
                request.getStartDate(),
                request.getEndDate()
        );

        if (statementExists) {
            throw new RuntimeException("Statement already exists for this period");
        }

        return generateStatementFromTransactions(transactions);
    }

    @Override
    public StatementDownloadDto downloadStatement(Long accountNumber, LocalDate periodStart, LocalDate periodEnd) {
        Statement statement = statementRepository.findByAccountNumberAndPeriodStartAndPeriodEnd(accountNumber, periodStart, periodEnd)
                .orElseThrow(() -> new RuntimeException("Statement is not available for this period"));

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

        return new StatementDownloadDto(statement.getStatementId(), downloadUrl, expiresAt);
    }

    @Override
    public Resource getStatementPdf(UUID statementId, String token) {
        if (!statementDownloadJwtService.isValidForStatement(statementId, token)) {
            throw new RuntimeException("Statement download link has expired or is invalid");
        }

        Statement statement = statementRepository.findByStatementId(statementId)
                .orElseThrow(() -> new RuntimeException("Statement not found"));

        FileSystemResource resource = new FileSystemResource(statement.getS3Key());

        if (!resource.exists()) {
            throw new RuntimeException("Statement PDF file is not available");
        }

        return resource;
    }

    @Override
    public void generateMonthlyStatements() {
        log.info("Monthly statement batch started");

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

    private Statement generateStatementFromTransactions(Transactions transactions) {
        UUID statementId = UUID.randomUUID();

        byte[] pdfBytes = pdfGenerateService.generatePdf(transactions);

        try {
            Path storageDirectory = Path.of(statementStorageLocation);
            Files.createDirectories(storageDirectory);

            Path pdfPath = storageDirectory.resolve(statementId + ".pdf");

            Files.write(
                    pdfPath,
                    pdfBytes,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            Statement statement = Statement.builder()
                    .statementId(statementId)
                    .accountNumber(transactions.getAccountNumber())
                    .periodStart(transactions.getPeriodStart())
                    .periodEnd(transactions.getPeriodEnd())
                    .s3Key(pdfPath.toString())
                    .generatedAt(LocalDateTime.now())
                    .build();

            Statement savedStatement = statementRepository.save(statement);

            log.info(
                    "Generated monthly statement for accountNumber={}, statementId={}, periodStart={}, periodEnd={}",
                    transactions.getAccountNumber(),
                    statementId,
                    transactions.getPeriodStart(),
                    transactions.getPeriodEnd()
            );

            return savedStatement;

        } catch (Exception e) {
            log.error(
                    "Failed to store monthly statement PDF for accountNumber={}, periodStart={}, periodEnd={}",
                    transactions.getAccountNumber(),
                    transactions.getPeriodStart(),
                    transactions.getPeriodEnd(),
                    e
            );
            throw new RuntimeException("Failed to store generated statement PDF", e);
        }
    }

}

