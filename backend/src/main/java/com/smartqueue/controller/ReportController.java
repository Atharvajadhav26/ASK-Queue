package com.smartqueue.controller;

import com.smartqueue.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.smartqueue.security.SecurityUtils;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam(required = false, defaultValue = "daily") String period) {
        byte[] pdf = reportService.generatePdfReport(SecurityUtils.getCurrentUserId(), period);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                .body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(@RequestParam(required = false, defaultValue = "daily") String period) {
        byte[] excel = reportService.generateExcelReport(SecurityUtils.getCurrentUserId(), period);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.xlsx\"")
                .body(excel);
    }
}
