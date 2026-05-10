package com.ganta.microservices.statements.util;

import com.ganta.microservices.statements.service.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatementBatchScheduler {
    private final StatementService statementService;

    //This will run once a month at 01:00 am
    @Scheduled(cron = "0 0 1 1 * *")
    public void generateMonthlyStatements() {
        log.info("Scheduled monthly statement generation triggered");
        statementService.generateMonthlyStatements();
    }
}
