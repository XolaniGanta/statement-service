package com.ganta.microservices.statements.service;

import com.ganta.microservices.statements.pojo.Transactions;

public interface PdfGenerateService {
    byte[] generatePdf(Transactions transactions);
}
