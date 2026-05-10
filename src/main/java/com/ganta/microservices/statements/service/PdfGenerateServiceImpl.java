package com.ganta.microservices.statements.service;

import com.ganta.microservices.statements.pojo.TransactionItems;
import com.ganta.microservices.statements.pojo.Transactions;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGenerateServiceImpl implements PdfGenerateService{
    @Override
    public byte[] generatePdf(Transactions transactions) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Account Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            document.add(new Paragraph("Account Number: " + transactions.getAccountNumber()));
            document.add(new Paragraph("Customer ID: " + transactions.getCustomerId()));
            document.add(new Paragraph("Period Start: " + transactions.getPeriodStart()));
            document.add(new Paragraph("Period End: " + transactions.getPeriodEnd()));
            document.add(new Paragraph("Currency: " + transactions.getCurrency()));
            document.add(new Paragraph("Opening Balance: " + transactions.getOpeningBalance()));
            document.add(new Paragraph("Closing Balance: " + transactions.getClosingBalance()));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);

            addHeader(table, "Date");
            addHeader(table, "Type");
            addHeader(table, "Amount");
            addHeader(table, "Description");
            addHeader(table, "Reference");

            if (transactions.getTransactions() != null && !transactions.getTransactions().isEmpty()) {
                for (TransactionItems tx : transactions.getTransactions()) {
                    table.addCell(String.valueOf(tx.getTimestamp()));
                    table.addCell(String.valueOf(tx.getType()));
                    table.addCell(String.valueOf(tx.getAmount()));
                    table.addCell(tx.getDescription() == null ? "" : tx.getDescription());
                    table.addCell(tx.getReference() == null ? "" : tx.getReference());
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No transactions available"));
                emptyCell.setColspan(5);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(emptyCell);
            }

            document.add(table);
            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return pdfBytes;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private void addHeader(PdfPTable table, String title) {
        Font font = new Font(Font.HELVETICA, 12, Font.BOLD);
        PdfPCell header = new PdfPCell(new Phrase(title, font));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

}

