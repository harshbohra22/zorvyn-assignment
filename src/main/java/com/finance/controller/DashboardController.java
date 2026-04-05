package com.finance.controller;

import com.finance.dto.response.*;
import com.finance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummary>> getSummary() {
        DashboardSummary summary = dashboardService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/category-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<CategorySummary>>> getCategorySummary() {
        List<CategorySummary> categorySummary = dashboardService.getCategorySummary();
        return ResponseEntity.ok(ApiResponse.success(categorySummary));
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<MonthlyTrend>>> getMonthlyTrends() {
        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<RecordResponse>>> getRecentActivity() {
        List<RecordResponse> recentActivity = dashboardService.getRecentActivity();
        return ResponseEntity.ok(ApiResponse.success(recentActivity));
    }
}
