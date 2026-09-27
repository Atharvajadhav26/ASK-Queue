package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.repository.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report generation service for PDF and Excel exports.
 * All data comes from actual database records.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final QueueRepository queueRepository;
    private final TokenRepository tokenRepository;

    public ReportService(QueueRepository queueRepository,
                         TokenRepository tokenRepository) {
        this.queueRepository = queueRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Generate PDF report for a given period.
     */
    public byte[] generatePdfReport(Long adminId, String period) {
        try {
            LocalDate startDate = getStartDate(period);
            List<ServiceQueue> queues = queueRepository.findByCreatedBy(adminId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD, new Color(30, 41, 59));
            Paragraph title = new Paragraph("Smart Queue System — Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            // Period
            com.lowagie.text.Font subFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.NORMAL, new Color(100, 116, 139));
            Paragraph periodPara = new Paragraph("Period: " + capitalize(period) + " (" + startDate + " to " + LocalDate.now() + ")", subFont);
            periodPara.setAlignment(Element.ALIGN_CENTER);
            periodPara.setSpacingAfter(20);
            document.add(periodPara);

            for (ServiceQueue queue : queues) {
                addQueueReportSection(document, queue, startDate);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addQueueReportSection(Document document, ServiceQueue queue, LocalDate startDate) throws DocumentException {
        List<Token> tokens = tokenRepository.findByQueue(queue).stream()
                .filter(t -> t.getJoinTime() != null && !t.getJoinTime().toLocalDate().isBefore(startDate))
                .collect(Collectors.toList());

        // Queue header
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 14, com.lowagie.text.Font.BOLD, new Color(79, 70, 229));
        Paragraph queueHeader = new Paragraph(queue.getName() + " (" + queue.getQueueId() + ")", headerFont);
        queueHeader.setSpacingBefore(15);
        queueHeader.setSpacingAfter(8);
        document.add(queueHeader);

        // Summary stats
        int total = tokens.size();
        int completed = (int) tokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();
        int cancelled = (int) tokens.stream().filter(t -> t.getStatus() == TokenStatus.CANCELLED).count();
        int skipped = (int) tokens.stream().filter(t -> t.getSkipCount() > 0).count();
        int expired = (int) tokens.stream().filter(t -> t.getStatus() == TokenStatus.EXPIRED).count();

        double avgWait = tokens.stream()
                .filter(t -> t.getCalledTime() != null && t.getJoinTime() != null)
                .mapToLong(t -> java.time.Duration.between(t.getJoinTime(), t.getCalledTime()).toMinutes())
                .average().orElse(0);

        double avgService = tokens.stream()
                .filter(t -> t.getServingStartTime() != null && t.getCompletedTime() != null)
                .mapToLong(t -> java.time.Duration.between(t.getServingStartTime(), t.getCompletedTime()).toMinutes())
                .average().orElse(0);

        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.NORMAL);
        document.add(new Paragraph("Total Customers: " + total, normalFont));
        document.add(new Paragraph("Completed: " + completed + " | Cancelled: " + cancelled +
                " | Skipped: " + skipped + " | Expired: " + expired, normalFont));
        document.add(new Paragraph(String.format("Avg Wait: %.1f min | Avg Service: %.1f min", avgWait, avgService), normalFont));

        // Token details table
        if (!tokens.isEmpty()) {
            Paragraph tablePara = new Paragraph(" ");
            tablePara.setSpacingBefore(5);
            document.add(tablePara);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15, 20, 15, 15, 15, 20});

            // Header
            com.lowagie.text.Font tableHeaderFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.BOLD, Color.WHITE);
            Color headerBg = new Color(79, 70, 229);
            addCell(table, "Token", tableHeaderFont, headerBg);
            addCell(table, "Customer", tableHeaderFont, headerBg);
            addCell(table, "Status", tableHeaderFont, headerBg);
            addCell(table, "Join Time", tableHeaderFont, headerBg);
            addCell(table, "Wait (min)", tableHeaderFont, headerBg);
            addCell(table, "Service (min)", tableHeaderFont, headerBg);

            // Rows
            com.lowagie.text.Font cellFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8, com.lowagie.text.Font.NORMAL);
            for (Token token : tokens.stream().limit(50).collect(Collectors.toList())) {
                addCell(table, token.getTokenNumber(), cellFont, null);
                addCell(table, token.getUser() != null ? token.getUser().getName() : "—", cellFont, null);
                addCell(table, token.getStatus().name(), cellFont, null);
                addCell(table, token.getJoinTime() != null ? token.getJoinTime().format(DATE_FMT) : "—", cellFont, null);

                long waitMins = 0;
                if (token.getCalledTime() != null && token.getJoinTime() != null) {
                    waitMins = java.time.Duration.between(token.getJoinTime(), token.getCalledTime()).toMinutes();
                }
                addCell(table, String.valueOf(waitMins), cellFont, null);

                long serviceMins = 0;
                if (token.getServingStartTime() != null && token.getCompletedTime() != null) {
                    serviceMins = java.time.Duration.between(token.getServingStartTime(), token.getCompletedTime()).toMinutes();
                }
                addCell(table, String.valueOf(serviceMins), cellFont, null);
            }

            document.add(table);
        }
    }

    private void addCell(PdfPTable table, String text, com.lowagie.text.Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor);
        }
        table.addCell(cell);
    }

    /**
     * Generate Excel report for a given period.
     */
    public byte[] generateExcelReport(Long adminId, String period) {
        try {
            LocalDate startDate = getStartDate(period);
            List<ServiceQueue> queues = queueRepository.findByCreatedBy(adminId);

            XSSFWorkbook workbook = new XSSFWorkbook();

            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            createSummarySheet(workbook, summarySheet, queues, startDate);

            // Individual queue sheets
            for (ServiceQueue queue : queues) {
                String sheetName = queue.getName().replaceAll("[^a-zA-Z0-9\\s]", "").trim();
                if (sheetName.length() > 25) sheetName = sheetName.substring(0, 25);
                Sheet sheet = workbook.createSheet(sheetName);
                createQueueSheet(workbook, sheet, queue, startDate);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate Excel report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void createSummarySheet(XSSFWorkbook workbook, Sheet sheet, List<ServiceQueue> queues, LocalDate startDate) {
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Smart Queue System — Summary Report");
        titleCell.setCellStyle(headerStyle);

        rowNum++; // Empty row

        // Headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Queue Name", "Queue ID", "Service Type", "Total Tokens", "Completed", "Cancelled", "Avg Wait (min)", "Avg Service (min)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (ServiceQueue queue : queues) {
            List<Token> tokens = tokenRepository.findByQueue(queue).stream()
                    .filter(t -> t.getJoinTime() != null && !t.getJoinTime().toLocalDate().isBefore(startDate))
                    .collect(Collectors.toList());

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(queue.getName());
            row.createCell(1).setCellValue(queue.getQueueId());
            row.createCell(2).setCellValue(queue.getServiceType());
            row.createCell(3).setCellValue(tokens.size());
            row.createCell(4).setCellValue(tokens.stream().filter(t -> t.getStatus() == TokenStatus.COMPLETED).count());
            row.createCell(5).setCellValue(tokens.stream().filter(t -> t.getStatus() == TokenStatus.CANCELLED).count());

            double avgWait = tokens.stream()
                    .filter(t -> t.getCalledTime() != null && t.getJoinTime() != null)
                    .mapToLong(t -> java.time.Duration.between(t.getJoinTime(), t.getCalledTime()).toMinutes())
                    .average().orElse(0);
            row.createCell(6).setCellValue(Math.round(avgWait * 10.0) / 10.0);

            double avgService = tokens.stream()
                    .filter(t -> t.getServingStartTime() != null && t.getCompletedTime() != null)
                    .mapToLong(t -> java.time.Duration.between(t.getServingStartTime(), t.getCompletedTime()).toMinutes())
                    .average().orElse(0);
            row.createCell(7).setCellValue(Math.round(avgService * 10.0) / 10.0);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createQueueSheet(XSSFWorkbook workbook, Sheet sheet, ServiceQueue queue, LocalDate startDate) {
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        List<Token> tokens = tokenRepository.findByQueue(queue).stream()
                .filter(t -> t.getJoinTime() != null && !t.getJoinTime().toLocalDate().isBefore(startDate))
                .collect(Collectors.toList());

        int rowNum = 0;
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Token", "Customer", "Status", "Priority", "Join Time", "Called Time",
                "Completed Time", "Wait (min)", "Service (min)", "Skip Count"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (Token token : tokens) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(token.getTokenNumber());
            row.createCell(1).setCellValue(token.getUser() != null ? token.getUser().getName() : "—");
            row.createCell(2).setCellValue(token.getStatus().name());
            row.createCell(3).setCellValue(token.getPriority().name());
            row.createCell(4).setCellValue(token.getJoinTime() != null ? token.getJoinTime().format(DATE_FMT) : "");
            row.createCell(5).setCellValue(token.getCalledTime() != null ? token.getCalledTime().format(DATE_FMT) : "");
            row.createCell(6).setCellValue(token.getCompletedTime() != null ? token.getCompletedTime().format(DATE_FMT) : "");

            long waitMins = 0;
            if (token.getCalledTime() != null && token.getJoinTime() != null) {
                waitMins = java.time.Duration.between(token.getJoinTime(), token.getCalledTime()).toMinutes();
            }
            row.createCell(7).setCellValue(waitMins);

            long serviceMins = 0;
            if (token.getServingStartTime() != null && token.getCompletedTime() != null) {
                serviceMins = java.time.Duration.between(token.getServingStartTime(), token.getCompletedTime()).toMinutes();
            }
            row.createCell(8).setCellValue(serviceMins);
            row.createCell(9).setCellValue(token.getSkipCount());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private LocalDate getStartDate(String period) {
        switch (period != null ? period.toLowerCase() : "daily") {
            case "weekly": return LocalDate.now().minusDays(7);
            case "monthly": return LocalDate.now().minusDays(30);
            case "daily":
            default: return LocalDate.now();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
