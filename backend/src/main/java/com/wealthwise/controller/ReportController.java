package com.wealthwise.controller;

import com.wealthwise.dto.ReportSummaryResponse;
import com.wealthwise.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired private ReportService reportService;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private String resolveMonth(String month) {
        return (month == null || month.isBlank())
                ? YearMonth.now().format(MONTH_FMT)
                : month;
    }

    /** JSON summary — used by the frontend preview cards */
    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryResponse> summary(
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(reportService.getSummary(resolveMonth(month)));
    }

    /** PDF download */
    @GetMapping(value = "/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> pdf(
            @RequestParam(required = false) String month) throws Exception {
        String m = resolveMonth(month);
        byte[] bytes = reportService.generatePdf(m);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"wealthwise-" + m + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    /** CSV download */
    @GetMapping(value = "/csv", produces = "text/csv")
    public ResponseEntity<String> csv(
            @RequestParam(required = false) String month) {
        String m = resolveMonth(month);
        String csv = reportService.generateCsv(m);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"wealthwise-" + m + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
