package com.aswin.financetracker.controller;

import com.aswin.financetracker.service.ReportService;
import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public String viewMonthlyReport(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        Model model
    ) {
        YearMonth target;
        if (year != null && month != null && month >= 1 && month <= 12) {
            target = YearMonth.of(year, month);
        } else {
            target = YearMonth.now();
        }

        Map<String, Object> reportData = reportService.getMonthlyReportData(target);
        model.addAllAttributes(reportData);
        model.addAttribute("selectedYear", target.getYear());
        model.addAttribute("selectedMonth", target.getMonthValue());
        model.addAttribute("activeTab", "reports");

        return "reports/index";
    }

    @GetMapping({"/export-csv", "/export"})
    public ResponseEntity<byte[]> exportCsv(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) throws IOException {
        YearMonth target;
        if (year != null && month != null && month >= 1 && month <= 12) {
            target = YearMonth.of(year, month);
        } else {
            target = YearMonth.now();
        }

        byte[] csvData = reportService.generateCsvReport(target);
        String filename = "finance-report-" + target.toString() + ".csv";

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvData);
    }
}
