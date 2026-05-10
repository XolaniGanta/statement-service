package com.ganta.microservices.statements.service.impl;

import com.ganta.microservices.statements.exception.StatementErrorCode;
import com.ganta.microservices.statements.exception.StatementException;
import com.ganta.microservices.statements.pojo.Transactions;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {

    private final ObjectMapper objectMapper;

    public List<Transactions> getAllTransactions() {
        try {
            ClassPathResource resource = new ClassPathResource("transactions.json");

            return objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

        } catch (Exception e) {
            throw new StatementException(StatementErrorCode.TRANSACTIONS_LOAD_FAILED, e);
        }
    }

    public Transactions getTransactions(
            Long accountNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        return getAllTransactions().stream()
                .filter(transactions -> accountNumber.equals(transactions.getAccountNumber()))
                .filter(transactions -> periodStart.equals(transactions.getPeriodStart()))
                .filter(transactions -> periodEnd.equals(transactions.getPeriodEnd()))
                .findFirst()
                .orElseThrow(() -> new StatementException(StatementErrorCode.TRANSACTIONS_NOT_FOUND));
    }
}

