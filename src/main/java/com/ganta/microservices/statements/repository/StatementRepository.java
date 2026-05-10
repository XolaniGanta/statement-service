package com.ganta.microservices.statements.repository;

import com.ganta.microservices.statements.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, Long> {

    Optional<Statement> findByAccountNumberAndPeriodStartAndPeriodEnd(
            Long accountNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    Optional<Statement> findByStatementId(UUID statementId);

    boolean existsByAccountNumberAndPeriodStartAndPeriodEnd(
            Long accountNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    );

}
