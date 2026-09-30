package com.bibliomanager.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.bibliomanager.models.Loan;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class PDFExporter {

    public static String exportLoansReport(List<Loan> loans, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ── Fonts ──
            Font titleFont  = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD,
                    new BaseColor(89, 116, 250));
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
                    BaseColor.WHITE);
            Font cellFont   = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL,
                    new BaseColor(50, 50, 50));
            Font subFont    = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL,
                    new BaseColor(100, 100, 100));

            // ── Title ──
            Paragraph title = new Paragraph("BiblioManager — Loans Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(6);
            document.add(title);

            Paragraph date = new Paragraph("Generated on: " + LocalDate.now(), subFont);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(20);
            document.add(date);

            // ── Summary ──
            long active   = loans.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count();
            long returned = loans.stream().filter(l -> "RETURNED".equals(l.getStatus())).count();
            long overdue  = loans.stream().filter(l -> "OVERDUE".equals(l.getStatus())).count();

            PdfPTable summary = new PdfPTable(3);
            summary.setWidthPercentage(60);
            summary.setHorizontalAlignment(Element.ALIGN_CENTER);
            summary.setSpacingAfter(20);

            addSummaryCell(summary, "Active: " + active,   new BaseColor(89, 116, 250));
            addSummaryCell(summary, "Returned: " + returned, new BaseColor(100, 180, 100));
            addSummaryCell(summary, "Overdue: " + overdue,  new BaseColor(220, 100, 100));
            document.add(summary);

            // ── Table ──
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2.5f, 1.5f, 1.5f, 1.5f, 1.2f});
            table.setSpacingBefore(10);

            // Header row
            String[] headers = {"Book", "Member", "Loan Date", "Due Date", "Return Date", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(89, 116, 250));
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderColor(BaseColor.WHITE);
                table.addCell(cell);
            }

            // Data rows
            boolean alternate = false;
            for (Loan loan : loans) {
                BaseColor rowColor = alternate
                        ? new BaseColor(240, 240, 250)
                        : BaseColor.WHITE;

                addTableCell(table, loan.getBookTitle(),   cellFont, rowColor);
                addTableCell(table, loan.getMemberName(),  cellFont, rowColor);
                addTableCell(table, loan.getLoanDate() != null
                        ? loan.getLoanDate().toString() : "-",   cellFont, rowColor);
                addTableCell(table, loan.getDueDate() != null
                        ? loan.getDueDate().toString() : "-",    cellFont, rowColor);
                addTableCell(table, loan.getReturnDate() != null
                        ? loan.getReturnDate().toString() : "-", cellFont, rowColor);

                // Status cell with color
                BaseColor statusColor;
                switch (loan.getStatus()) {
                    case "OVERDUE":   statusColor = new BaseColor(220, 100, 100); break;
                    case "RETURNED":  statusColor = new BaseColor(100, 180, 100); break;
                    default:          statusColor = new BaseColor(89, 116, 250);  break;
                }
                Font statusFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
                PdfPCell statusCell = new PdfPCell(new Phrase(loan.getStatus(), statusFont));
                statusCell.setBackgroundColor(statusColor);
                statusCell.setPadding(6);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(statusCell);

                alternate = !alternate;
            }

            document.add(table);

            // ── Footer ──
            Paragraph footer = new Paragraph(
                    "\nTotal loans: " + loans.size(), subFont
            );
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return filePath;

        } catch (Exception e) {
            System.err.println("PDF export error: " + e.getMessage());
            return null;
        }
    }

    private static void addSummaryCell(PdfPTable table, String text, BaseColor color) {
        Font f = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(color);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private static void addTableCell(PdfPTable table, String text,
                                     Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}