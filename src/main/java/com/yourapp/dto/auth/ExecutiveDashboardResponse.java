package com.yourapp.dto.auth;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ExecutiveDashboardResponse(

        // Overview statistics
        long activeObjectiveCount,
        long departmentCount,
        long overdueTaskCount,

        // Lists
        List<ObjectiveSummary> companyObjectives,
        List<DepartmentSummary> departments,
        List<KpiSummary> kpis,
        List<OverdueTaskSummary> overdueTasks

) {

    public record ObjectiveSummary(
            UUID id,
            String title,
            String status,
            LocalDate endDate
    ) {}

    public record DepartmentSummary(
            UUID id,
            String name,
            String description
    ) {}

    public record KpiSummary(
            UUID id,
            String name,
            BigDecimal targetValue,
            String unit,
            String frequency
    ) {}

    public record OverdueTaskSummary(
            UUID id,
            String title,
            String status,
            LocalDate dueDate
    ) {}
}