package com.chubb.claims.dashboard.api;

import com.chubb.claims.dashboard.dto.DashboardMetricsResponse;
import com.chubb.claims.dashboard.dto.LiabilityExposureResponse;
import com.chubb.claims.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Workload and Multi-Currency Liability Exposure Analytics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Get aggregated workload and claim status counts")
    public ResponseEntity<DashboardMetricsResponse> getWorkloadMetrics() {
        return ResponseEntity.ok(dashboardService.getWorkloadMetrics());
    }

    @GetMapping("/liability")
    @Operation(summary = "Get statutory currency liability exposure across all 6 APAC markets")
    public ResponseEntity<LiabilityExposureResponse> getLiabilityExposure() {
        return ResponseEntity.ok(dashboardService.getLiabilityExposure());
    }
}
