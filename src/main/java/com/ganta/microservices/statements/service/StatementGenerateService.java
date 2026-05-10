package com.ganta.microservices.statements.service;

import com.ganta.microservices.statements.pojo.Transactions;

public interface StatementGenerateService {
    byte[] generatePdf(Transactions transactions);
}
