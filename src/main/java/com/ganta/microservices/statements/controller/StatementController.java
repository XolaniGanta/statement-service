package com.ganta.microservices.statements.controller;

import com.ganta.microservices.statements.model.Statement;
import com.ganta.microservices.statements.pojo.GenerateStatementRequest;
import com.ganta.microservices.statements.pojo.GenerateStatementResponse;
import com.ganta.microservices.statements.pojo.StatementDownloadDto;
import com.ganta.microservices.statements.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/statements")
@RequiredArgsConstructor
public class StatementController {

    private final StatementService statementService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateStatementResponse> generateStatement(@RequestBody GenerateStatementRequest request) {
        GenerateStatementResponse statement = statementService.generateStatement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(statement);
    }

    @GetMapping("/download")
    public ResponseEntity<StatementDownloadDto> downloadStatement(
            @RequestParam Long accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {

        StatementDownloadDto response = statementService.downloadStatement(accountNumber, periodStart, periodEnd);

        return ResponseEntity.ok(response);
    }

    @GetMapping(
            value = "/{statementId}/file",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<Resource> openStatementPdf(@PathVariable UUID statementId, @RequestParam String token) {
        Resource resource = statementService.getStatementPdf(statementId, token);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PostMapping("/batch/generate")
    public ResponseEntity<String> generateMonthlyStatements() {
        statementService.generateMonthlyStatements();
        return ResponseEntity.ok("Monthly statement batch completed");
    }
}